package net.cytonic.cytosis.replay.io;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import net.minestom.server.network.NetworkBuffer;

import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.replay.ReplayEvent;
import net.cytonic.cytosis.replay.instance.InstanceRecorder;

/**
 * Short-lived in-memory event accumulator, one per {@link InstanceRecorder}.
 */
public class MemoryBuffer {

    private static final int MAX_EVENTS = 300;
    private final List<ReplayEvent> events = new ArrayList<>();
    private Instant lastFlush = Instant.now();

    public void addEvent(ReplayEvent event) {
        if (event == null) {
            Logger.warn("Attempted to log null replay event!", new Exception());
            return;
        }
        events.add(event);
    }

    public boolean shouldFlush() {
        return events.size() >= MAX_EVENTS || lastFlush.plusSeconds(5).isBefore(Instant.now());
    }

    /**
     * Serializes everything currently buffered and clears state.
     */
    public byte[] drainAndSerialize() {
        if (events.isEmpty()) return new byte[0];
        List<ReplayEvent> snapshot = new ArrayList<>(events);
        events.clear();
        lastFlush = Instant.now();
        return NetworkBuffer.makeArray(buf -> {
            buf.write(NetworkBuffer.VAR_INT, snapshot.size());
            for (ReplayEvent event : snapshot) {
                if (event == null) continue;
                buf.write(ReplayEvent.NETWORK_TYPE, event);
            }
        });
    }
}