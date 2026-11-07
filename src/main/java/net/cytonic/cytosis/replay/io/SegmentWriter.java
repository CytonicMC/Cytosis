package net.cytonic.cytosis.replay.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.replay.ReplayBundle;
import net.cytonic.cytosis.replay.ReplayEvent;
import net.cytonic.cytosis.replay.ReplayEvent.BlockUpdate;
import net.cytonic.cytosis.replay.ReplayEvent.EntityRemove;
import net.cytonic.cytosis.replay.ReplayEvent.EntitySpawn;
import net.cytonic.cytosis.replay.ReplayEvent.EquipmentUpdate;
import net.cytonic.cytosis.replay.ReplayEvent.MetaUpdate;
import net.cytonic.cytosis.replay.ReplayEvent.PlayerIdent;
import net.cytonic.cytosis.replay.ReplayEvent.PlayerJoin;
import net.cytonic.cytosis.replay.ReplayEvent.PlayerLeave;
import net.cytonic.cytosis.replay.ReplayEvent.PosRot;
import net.cytonic.cytosis.replay.ReplayMeta;

public class SegmentWriter {

    /**
     * Change this value to adjust how long a segment lasts before it is rotated. Deltas are recorded relative to the
     * previous tick, so a segment should be long enough to make rotation overhead worthwhile, but short enough that
     * exports do not need to read excessive history. {@link Segment#overlaps(Instant, Instant)} uses this value.
     */
    public static final Duration SEGMENT_DURATION = Duration.ofMinutes(10);
    public static final Duration RETENTION_WINDOW = Duration.ofMinutes(60);

    private final Path segmentDir;
    private final Object writeLock = new Object();
    private final Object fileReadLock = new Object();
    private final ExecutorService ioExecutor;
    private final ScheduledExecutorService scheduler;
    private volatile Segment currentSegment;
    private volatile boolean closed;

    @Setter
    private Consumer<Void> onRotate;

    public SegmentWriter(Path segmentDir) {
        this.segmentDir = segmentDir;
        try {
            Files.createDirectories(segmentDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.ioExecutor = Executors.newSingleThreadExecutor(daemonFactory("replay-writer-" + segmentDir.getFileName()));
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                daemonFactory("replay-rotator-" + segmentDir.getFileName()));
        this.currentSegment = openNewSegment();

        long rotationPeriod = Math.max(1_000L, SEGMENT_DURATION.toMillis());
        scheduler.scheduleAtFixedRate(this::rotate, rotationPeriod, rotationPeriod, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::evictOldSegments, 10, 10, TimeUnit.MINUTES);
    }

    private static ThreadFactory daemonFactory(String name) {
        return runnable -> {
            Thread thread = new Thread(runnable, name);
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * Non-blocking: hands the chunk off to the ordered single-threaded writer and returns immediately.
     */
    @NonBlocking
    public CompletableFuture<Void> appendAsync(byte[] serializedChunk) {
        if (closed || serializedChunk == null || serializedChunk.length == 0) {
            return CompletableFuture.completedFuture(null);
        }
        synchronized (writeLock) {
            if (closed) return CompletableFuture.completedFuture(null);
            Segment segment = currentSegment;
            try {
                return CompletableFuture.runAsync(() -> segment.append(serializedChunk), ioExecutor)
                        .exceptionally(throwable -> {
                            Logger.error("Failed to write replay chunk!", throwable);
                            return null;
                        });
            } catch (RuntimeException e) {
                Logger.error("Failed to queue replay chunk!", e);
                return CompletableFuture.completedFuture(null);
            }
        }
    }

    @Blocking
    public void appendSync(byte[] serializedChunk) {
        appendAsync(serializedChunk).join();
    }

    /**
     * Waits for all previously submitted chunks to be written. This is a barrier, not a flush to disk.
     */
    @Blocking
    public void awaitIdle() {
        if (closed) return;
        try {
            CompletableFuture.runAsync(() -> {
            }, ioExecutor).join();
        } catch (RuntimeException e) {
            Logger.error("Failed to wait for replay writes!", e);
        }
    }

    private void rotate() {
        if (closed) return;
        Segment fresh;
        try {
            fresh = openNewSegment();
        } catch (RuntimeException e) {
            Logger.error("Failed to open a new replay segment!", e);
            return;
        }
        synchronized (writeLock) {
            if (closed) {
                fresh.close();
                return;
            }
            Segment old = currentSegment;
            // Set the recorder's baseline flags synchronously before anything can be written to the new segment.
            if (onRotate != null) onRotate.accept(null);
            currentSegment = fresh;
            // The executor is single-threaded, so this close happens after every chunk already submitted to `old`.
            try {
                ioExecutor.execute(old::close);
            } catch (RuntimeException e) {
                // The writer may be shutting down concurrently; do not leak the old segment if the queue rejects.
                old.close();
            }
        }
    }

    private void evictOldSegments() {
        if (closed) return;
        try {
            ioExecutor.execute(() -> {
                if (closed) return;
                Instant cutoff = Instant.now().minus(RETENTION_WINDOW);
                Segment active = currentSegment;
                synchronized (fileReadLock) {
                    for (Path path : listSegmentFiles()) {
                        try {
                            if (active != null && path.equals(active.getPath())) continue;
                            if (Segment.createdAtOf(path).isBefore(cutoff)) {
                                Files.deleteIfExists(path);
                            }
                        } catch (Exception e) {
                            Logger.warn("Failed to delete old recording segment " + path, e);
                        }
                    }
                }
            });
        } catch (RuntimeException e) {
            Logger.warn("Failed to queue replay segment eviction", e);
        }
    }

    private List<Path> listSegmentFiles() {
        List<Path> paths = new ArrayList<>();
        try (var files = Files.list(segmentDir)) {
            files.filter(Segment::isSegmentFile).forEach(paths::add);
        } catch (IOException e) {
            Logger.warn("Failed to list replay segments", e);
        }
        return paths;
    }

    private Segment openNewSegment() {
        return Segment.create(segmentDir, Instant.now());
    }

    public ReplayBundle exportRange(Instant from, Instant to, int fromTick, int toTick, Key map) {
        List<ReplayEvent> allEvents = new ArrayList<>();
        // Read retained history so the exported window can start with a complete baseline, not just the events that
        // happen to fall inside [fromTick, toTick].
        synchronized (fileReadLock) {
            for (Segment segment : findSegmentsUpTo(to)) {
                segment.readInto(allEvents, Integer.MIN_VALUE, toTick);
            }
        }
        allEvents.sort(Comparator.comparingInt(ReplayEvent::tick));

        ExportState state = new ExportState();
        List<ReplayEvent> window = new ArrayList<>();
        for (ReplayEvent event : allEvents) {
            if (event.tick() < fromTick) {
                state.apply(event);
            } else {
                window.add(event);
            }
        }

        List<ReplayEvent> events = new ArrayList<>(window.size() + 64);
        events.add(new PlayerIdent(fromTick, state.idents()));
        events.addAll(state.baseline(fromTick));
        events.addAll(window);

        ReplayMeta meta = new ReplayMeta(map, Cytosis.CONTEXT.SERVER_ID, null, Instant.now(), from);
        return new ReplayBundle(events, meta);
    }

    private List<Segment> findSegmentsUpTo(Instant to) {
        List<Segment> segments = new ArrayList<>();
        for (Path path : listSegmentFiles()) {
            try {
                Segment segment = Segment.open(path);
                if (!segment.getCreatedAt().isAfter(to)) {
                    segments.add(segment);
                }
            } catch (RuntimeException e) {
                Logger.warn("Skipping unreadable replay segment " + path, e);
            }
        }
        segments.sort(Comparator.comparing(Segment::getCreatedAt));
        return segments;
    }

    /**
     * Releases the writer's threads and closes the active segment. Pending writes are allowed to finish first.
     */
    public void close() {
        if (closed) return;
        closed = true;
        scheduler.shutdownNow();
        try {
            // Queue the close after every chunk submitted before `closed` became visible.
            synchronized (writeLock) {
                ioExecutor.execute(() -> {
                    synchronized (fileReadLock) {
                        synchronized (writeLock) {
                            currentSegment.close();
                        }
                    }
                });
            }
            ioExecutor.shutdown();
            if (!ioExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                Logger.warn("Replay writer for %s did not terminate in time", segmentDir);
                ioExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Logger.warn("Interrupted while closing replay writer for " + segmentDir, e);
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            Logger.warn("Failed to close replay writer for " + segmentDir, e);
            ioExecutor.shutdownNow();
        }
    }

    /**
     * Folds historical events into the state a replay must start with. This is intentionally based on the recorded
     * event stream rather than the live world, so a report window is self-contained.
     */
    private static final class ExportState {

        private final Map<Integer, UUID> idents = new HashMap<>();
        private final Set<Integer> activeEntities = new HashSet<>();
        private final Map<Integer, EntitySpawn> spawns = new HashMap<>();
        private final Map<Integer, Pos> positions = new HashMap<>();
        private final Map<Integer, Map<EquipmentSlot, ItemStack>> equipment = new HashMap<>();
        private final Map<Integer, Map<Integer, Metadata.Entry<?>>> metadata = new HashMap<>();
        private final Map<Point, Block> originalBlocks = new HashMap<>();
        private final Map<Point, Block> blocks = new HashMap<>();

        void apply(ReplayEvent event) {
            if (event instanceof PlayerIdent e) {
                idents.putAll(e.idents());
                activeEntities.addAll(e.idents().keySet());
            } else if (event instanceof PlayerJoin e) {
                activeEntities.add(e.entityId());
            } else if (event instanceof PlayerLeave e) {
                removeEntity(e.entityId());
            } else if (event instanceof EntitySpawn e) {
                spawns.put(e.entityId(), e);
                activeEntities.add(e.entityId());
                positions.putIfAbsent(e.entityId(), e.position());
            } else if (event instanceof EntityRemove e) {
                removeEntity(e.entityId());
            } else if (event instanceof PosRot e) {
                e.entries().forEach((entityId, snapshot) -> {
                    if (snapshot.isAbsolute()) {
                        positions.put(entityId, snapshot.toPos());
                        return;
                    }
                    Pos previous = positions.get(entityId);
                    if (previous == null) return;
                    positions.put(entityId, new Pos(
                            previous.x() + snapshot.dx() / 10_000.0,
                            previous.y() + snapshot.dy() / 10_000.0,
                            previous.z() + snapshot.dz() / 10_000.0,
                            snapshot.yaw(), snapshot.pitch()));
                });
            } else if (event instanceof EquipmentUpdate e) {
                equipment.computeIfAbsent(e.entityId(), _ -> new HashMap<>()).putAll(e.slots());
            } else if (event instanceof MetaUpdate e) {
                metadata.computeIfAbsent(e.entityId(), _ -> new HashMap<>()).putAll(e.entries());
            } else if (event instanceof BlockUpdate e) {
                originalBlocks.putIfAbsent(e.pos(), e.existing());
                blocks.put(e.pos(), e.block());
            }
        }

        private void removeEntity(int entityId) {
            idents.remove(entityId);
            activeEntities.remove(entityId);
            spawns.remove(entityId);
            positions.remove(entityId);
            equipment.remove(entityId);
            metadata.remove(entityId);
        }

        Map<Integer, UUID> idents() {
            return new HashMap<>(idents);
        }

        List<ReplayEvent> baseline(int tick) {
            List<ReplayEvent> baseline = new ArrayList<>();
            // Players already in the instance at the start of the window need an explicit join. A later join event
            // in the window (for example, after the player leaves and reconnects) must not hide that initial state.
            for (Map.Entry<Integer, UUID> entry : idents.entrySet()) {
                if (activeEntities.contains(entry.getKey())) {
                    baseline.add(new PlayerJoin(tick, entry.getKey()));
                }
            }
            for (EntitySpawn spawn : spawns.values()) {
                if (!activeEntities.contains(spawn.entityId())) continue;
                baseline.add(new EntitySpawn(tick, spawn.entityId(), spawn.uuid(), spawn.type(),
                        spawn.position(), spawn.velocity()));
            }

            Map<Integer, Pos> activePositions = new HashMap<>();
            for (Map.Entry<Integer, Pos> entry : positions.entrySet()) {
                if (activeEntities.contains(entry.getKey())) {
                    activePositions.put(entry.getKey(), entry.getValue());
                }
            }
            if (!activePositions.isEmpty()) {
                baseline.add(new PosRot(activePositions, new HashSet<>(activePositions.keySet()), tick));
            }

            for (Map.Entry<Integer, Map<EquipmentSlot, ItemStack>> entry : equipment.entrySet()) {
                if (!activeEntities.contains(entry.getKey()) || entry.getValue().isEmpty()) continue;
                baseline.add(new EquipmentUpdate(tick, entry.getKey(), new HashMap<>(entry.getValue())));
            }

            for (Map.Entry<Integer, Map<Integer, Metadata.Entry<?>>> entry : metadata.entrySet()) {
                if (!activeEntities.contains(entry.getKey()) || entry.getValue().isEmpty()) continue;
                baseline.add(new MetaUpdate(tick, entry.getKey(), new HashMap<>(entry.getValue())));
            }

            for (Map.Entry<Point, Block> entry : blocks.entrySet()) {
                Block original = originalBlocks.get(entry.getKey());
                if (original != null && original.compare(entry.getValue())) continue;
                baseline.add(new BlockUpdate(tick, entry.getKey(), entry.getValue(), original));
            }
            return baseline;
        }
    }
}
