package net.cytonic.cytosis.replay.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.Setter;
import net.kyori.adventure.key.Key;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.replay.ReplayBundle;
import net.cytonic.cytosis.replay.ReplayEvent;
import net.cytonic.cytosis.replay.ReplayMeta;


public class SegmentWriter {

    private static final Duration SEGMENT_DURATION = Duration.ofMinutes(10);
    private static final Duration RETENTION_WINDOW = Duration.ofMinutes(60);

    private final Path segmentDir;
    private final ExecutorService ioExecutor = Executors.newCachedThreadPool();

    @Setter
    private Consumer<Void> onRotate;

    private volatile Segment currentSegment;

    public SegmentWriter(Path segmentDir) {
        this.segmentDir = segmentDir;
        if (!segmentDir.toFile().exists()) {
            segmentDir.toFile().mkdirs();
        }
        this.currentSegment = openNewSegment();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::rotate, SEGMENT_DURATION.toSeconds(),
            SEGMENT_DURATION.toSeconds(), TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::evictOldSegments, 10, 10, TimeUnit.MINUTES);
    }

    /**
     * Non-blocking: hands the chunk off and returns immediately.
     */
    public void appendAsync(byte[] serializedChunk) {
        ioExecutor.submit(() -> appendSync(serializedChunk));
    }

    public void appendSync(byte[] serializedChunk) {
        try {
            currentSegment.append(serializedChunk);
        } catch (Exception e) {
            Logger.error("Failed to write chunk!", e);
        }
    }

    private synchronized void rotate() {
        ioExecutor.submit(() -> {
            currentSegment.close();
            currentSegment = openNewSegment();
            if (onRotate != null) onRotate.accept(null);
        });
    }

    private void evictOldSegments() {
        ioExecutor.submit(() -> {
            Instant cutoff = Instant.now().minus(RETENTION_WINDOW);
            try (var files = Files.list(segmentDir)) {
                files.filter(p -> Segment.createdAtOf(p).isBefore(cutoff))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                            Logger.warn("Failed to delete old recording segment:");
                        }
                    });
            } catch (IOException ignored) {
            }
        });
    }

    private Segment openNewSegment() {
        return Segment.create(segmentDir, Instant.now());
    }

    public ReplayBundle exportRange(Instant from, Instant to, int fromTick, int toTick, Key map) {
        List<Segment> overlapping = findSegmentsOverlapping(from, to); // wall-clock filter, coarse

        List<ReplayEvent> allEvents = new ArrayList<>();
        for (Segment seg : overlapping) {
            seg.readInto(allEvents, fromTick, toTick); // tick filter, exact
        }

        ReplayMeta meta = new ReplayMeta(map, Cytosis.CONTEXT.SERVER_ID, null, Instant.now(),
            from); // or however you're sourcing this
        return new ReplayBundle(allEvents, meta);
    }

    private List<Segment> findSegmentsOverlapping(Instant from, Instant to) {
        try (var files = Files.list(segmentDir)) {
            return files
                .map(Segment::open)
                .filter(s -> s.overlaps(from, to))
                .sorted(Comparator.comparing(Segment::getCreatedAt))
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
