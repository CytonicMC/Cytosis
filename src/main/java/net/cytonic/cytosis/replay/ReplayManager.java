package net.cytonic.cytosis.replay;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.replay.instance.ReplayInstance;

@CytosisComponent
public class ReplayManager {

    private final Map<UUID, UUID> activePlaybacks = new HashMap<>();

    public boolean isPlaying(UUID recording) {
        return activePlaybacks.containsKey(recording);
    }

    public void removePlayback(UUID recording) {
        activePlaybacks.remove(recording);
    }

    public void registerPlayback(UUID recording, UUID instance) {
        activePlaybacks.put(recording, instance);
    }

    @Nullable
    public ReplayInstance getPlayback(UUID recording) {
        if (!isPlaying(recording)) return null;
        return (ReplayInstance) Cytosis.get(InstanceManager.class).getInstance(activePlaybacks.get(recording));
    }

}
