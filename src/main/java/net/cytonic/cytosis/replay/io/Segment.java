package net.cytonic.cytosis.replay.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

import lombok.Getter;
import net.minestom.server.network.NetworkBuffer;

import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.replay.ReplayEvent;

/**
 * One rotation window's worth of recording data for an instance.
 * <p>
 * File format:
 * <pre>
 * [int magic 'CYTR'][int format version][long segment creation epoch millis]
 * then repeated framed chunks:
 * [VAR_INT payload length][payload]
 * </pre>
 * The payload is exactly what {@link MemoryBuffer#drainAndSerialize()} produces:
 * {@code [VAR_INT eventCount][event...][event...]}. Chunks are length-prefixed so a partially written tail can be
 * detected and ignored instead of corrupting the whole segment.
 * <p>
 * Ticks embedded in events are absolute (since InstanceRecorder's counter never resets), so no per-segment tick-offset
 * math is needed — only startTime is used, and only for approximate overlap/eviction checks.
 */
public class Segment {

    public static final int FORMAT_VERSION = 2;

    /**
     * "CYTR"
     */
    private static final int MAGIC = 0x43595452;
    private static final int HEADER_SIZE = Integer.BYTES + Integer.BYTES + Long.BYTES;

    @Getter
    private final Path path;
    @Getter
    private final Instant createdAt;
    private OutputStream activeWriter;

    private Segment(Path path, Instant createdAt, OutputStream activeWriter) {
        this.path = path;
        this.createdAt = createdAt;
        this.activeWriter = activeWriter;
    }

    public static Segment create(Path dir, Instant createdAt) {
        Path path = dir.resolve(createdAt.toEpochMilli() + ".seg");
        try {
            OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW);
            out.write(NetworkBuffer.makeArray(buf -> {
                buf.write(NetworkBuffer.INT, MAGIC);
                buf.write(NetworkBuffer.INT, FORMAT_VERSION);
                buf.write(NetworkBuffer.LONG, createdAt.toEpochMilli());
            }));
            return new Segment(path, createdAt, out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Segment open(Path path) {
        try {
            byte[] header = new byte[HEADER_SIZE];
            try (var in = Files.newInputStream(path)) {
                int read = in.readNBytes(header, 0, header.length);
                if (read < header.length) {
                    throw new IOException("Truncated segment header: " + path);
                }
            }
            NetworkBuffer buf = NetworkBuffer.wrap(header, 0, header.length);
            int magic = buf.read(NetworkBuffer.INT);
            if (magic != MAGIC) {
                throw new IOException("Invalid replay segment magic in " + path);
            }
            int version = buf.read(NetworkBuffer.INT);
            if (version != FORMAT_VERSION) {
                throw new IOException("Unsupported replay segment version " + version + " in " + path);
            }
            Instant createdAt = Instant.ofEpochMilli(buf.read(NetworkBuffer.LONG));
            return new Segment(path, createdAt, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Instant createdAtOf(Path path) {
        String name = path.getFileName().toString();
        if (!name.endsWith(".seg")) {
            throw new IllegalArgumentException("Not a replay segment: " + path);
        }
        return Instant.ofEpochMilli(Long.parseLong(name.substring(0, name.length() - 4)));
    }

    public static boolean isSegmentFile(Path path) {
        return path.getFileName().toString().endsWith(".seg");
    }

    public boolean overlaps(Instant from, Instant to) {
        Instant approxEnd = createdAt.plus(SegmentWriter.SEGMENT_DURATION);
        return !createdAt.isAfter(to) && !approxEnd.isBefore(from);
    }

    public synchronized void append(byte[] serializedChunk) {
        if (activeWriter == null) throw new IllegalStateException("Segment is closed for writing: " + path);
        if (serializedChunk.length == 0) return;
        try {
            // BYTE_ARRAY is a VAR_INT length followed by the raw bytes: exactly the chunk framing we read below.
            byte[] framed = NetworkBuffer.makeArray(buf -> buf.write(NetworkBuffer.BYTE_ARRAY, serializedChunk));
            activeWriter.write(framed);
            activeWriter.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public synchronized void close() {
        if (activeWriter == null) return;
        try {
            activeWriter.close();
        } catch (IOException ignored) {
        } finally {
            activeWriter = null;
        }
    }

    public void readInto(List<ReplayEvent> eventsOut, int fromTick, int toTick) {
        for (ReplayEvent event : readAll()) {
            int tick = event.tick();
            if (tick >= fromTick && tick <= toTick) {
                eventsOut.add(event);
            }
        }
    }

    public List<ReplayEvent> readAll() {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (bytes.length < HEADER_SIZE) {
            Logger.warn("Ignoring truncated replay segment: %s", path);
            return List.of();
        }

        NetworkBuffer buffer = NetworkBuffer.wrap(bytes, 0, bytes.length);
        List<ReplayEvent> events = new java.util.ArrayList<>();
        try {
            int magic = buffer.read(NetworkBuffer.INT);
            int version = buffer.read(NetworkBuffer.INT);
            buffer.read(NetworkBuffer.LONG); // createdAt, already known
            if (magic != MAGIC || version != FORMAT_VERSION) {
                Logger.warn("Ignoring incompatible replay segment: %s", path);
                return List.of();
            }

            while (buffer.readableBytes() > 0) {
                int length;
                try {
                    length = buffer.read(NetworkBuffer.VAR_INT);
                } catch (RuntimeException e) {
                    break; // incomplete length prefix at the tail
                }
                if (length < 0 || length > buffer.readableBytes()) {
                    Logger.warn("Ignoring incomplete replay chunk at the end of %s", path);
                    break;
                }
                byte[] payload = buffer.read(NetworkBuffer.FixedRawBytes(length));
                NetworkBuffer chunk = NetworkBuffer.wrap(payload, 0, payload.length);
                int count = chunk.read(NetworkBuffer.VAR_INT);
                for (int i = 0; i < count; i++) {
                    events.add(chunk.read(ReplayEvent.NETWORK_TYPE));
                }
            }
        } catch (RuntimeException e) {
            Logger.warn("Failed to read replay segment " + path, e);
        }
        return events;
    }
}
