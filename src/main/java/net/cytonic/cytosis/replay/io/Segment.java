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

import net.cytonic.cytosis.replay.ReplayEvent;

/**
 * One rotation window's worth of recording data for an instance.
 * <p>
 * File format: [long: segment creation epoch millis]  (written once, at creation) repeated chunks, each exactly what
 * {@link MemoryBuffer#drainAndSerialize()} produces: [VAR_INT eventCount][event...][event...]...
 * <p>
 * Chunks are simply concatenated. no chunk framing needed, since each chunk is self-terminating (reader knows how many
 * events to consume from the count prefix) and reading just loops until EOF.
 * <p>
 * Ticks embedded in events are absolute (since InstanceRecorder's counter never resets), so no per-segment tick-offset
 * math is needed — only startTime is used, and only for approximate overlap/eviction checks.
 */
public class Segment {

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
            out.write(NetworkBuffer.makeArray(buf ->
                buf.write(NetworkBuffer.LONG, createdAt.toEpochMilli())
            ));
            return new Segment(path, createdAt, out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Segment open(Path path) {
        try {
            byte[] header = new byte[8]; // just the long now
            try (var in = Files.newInputStream(path)) {
                int read = in.readNBytes(header, 0, header.length);
                if (read < header.length) {
                    throw new IOException("Truncated segment header: " + path);
                }
            }
            NetworkBuffer buf = NetworkBuffer.wrap(header, 0, header.length);
            Instant createdAt = Instant.ofEpochMilli(buf.read(NetworkBuffer.LONG));
            return new Segment(path, createdAt, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Instant createdAtOf(Path path) {
        String name = path.getFileName().toString().replace(".seg", "");
        return Instant.ofEpochMilli(Long.parseLong(name));
    }

    public boolean overlaps(Instant from, Instant to) {
        Instant approxEnd = createdAt.plusSeconds(60);
        return !createdAt.isAfter(to) && !approxEnd.isBefore(from);
    }

    public void append(byte[] serializedChunk) {
        if (activeWriter == null) throw new IllegalStateException("Segment is closed for writing: " + path);
        try {
            activeWriter.write(serializedChunk);
            activeWriter.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void close() {
        if (activeWriter == null) return;
        try {
            activeWriter.close();
        } catch (IOException ignored) {
        } finally {
            activeWriter = null;
        }
    }

    public void readInto(List<ReplayEvent> eventsOut, int fromTick, int toTick) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        NetworkBuffer buffer = NetworkBuffer.wrap(bytes, 0, bytes.length);
        buffer.read(NetworkBuffer.LONG); // createdAt, already known

        while (buffer.readableBytes() > 0) {
            int count = buffer.read(NetworkBuffer.VAR_INT);
            for (int i = 0; i < count; i++) {
                ReplayEvent event = buffer.read(ReplayEvent.NETWORK_TYPE);
                int t = event.tick();
                if (t >= fromTick && t <= toTick) {
                    eventsOut.add(event);
                }
            }
        }
    }
}
