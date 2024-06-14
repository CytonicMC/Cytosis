package net.cytonic.cytosis.managers;

import net.cytonic.cytosis.npcs.NPC;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NPCManager {
    private final List<NPC> npcs = new ArrayList<>();

    public void addNPC(NPC npc) {
        npcs.add(npc);
    }

    public void removeNPC(NPC npc) {
        npcs.remove(npc);
    }

    @NotNull
    public Optional<NPC> findNPC(UUID id) {
        for (NPC npc : npcs) {
            if (npc.getUUID().equals(id)) {
                return Optional.of(npc);
            }
        }
        return Optional.empty();
    }
}
