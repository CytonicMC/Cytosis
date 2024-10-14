package net.cytonic.cytosis.managers;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.npcs.NPC;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A class that manages NPCs
 */
@NoArgsConstructor
public class NPCManager {

    private final List<NPC> npcs = new ArrayList<>();

    /**
     * Adds an NPC to the manager
     *
     * @param npc the NPC to add
     */
    public void addNPC(NPC npc) {
        npcs.add(npc);
    }

    /**
     * Removes an NPC from the manager
     * @param npc the NPC to remove
     */
    public void removeNPC(NPC npc) {
        npcs.remove(npc);
    }

    /**
     * Finds an NPC by UUID
     * @param id the uuid to find the NPC by
     * @return An optional of the NPC
     */
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
