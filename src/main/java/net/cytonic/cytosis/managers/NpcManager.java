package net.cytonic.cytosis.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.npcs.Npc;

/**
 * A class that manages NPCs
 */
@CytosisComponent
@NoArgsConstructor
public class NpcManager {

    private final List<Npc> npcs = new ArrayList<>();

    /**
     * Adds an NPC to the manager
     *
     * @param npc the NPC to add
     */
    public void addNpc(Npc npc) {
        npcs.add(npc);
    }

    /**
     * Removes an NPC from the manager
     *
     * @param npc the NPC to remove
     */
    public void removeNpc(Npc npc) {
        npc.remove();
        npcs.remove(npc);
    }

    /**
     * Finds an NPC by UUID
     *
     * @param id the uuid to find the NPC by
     * @return An optional of the NPC
     */
    @NotNull
    public Optional<Npc> findNpc(UUID id) {
        for (Npc npc : npcs) {
            if (npc.getUuid().equals(id)) {
                return Optional.of(npc);
            }
        }
        return Optional.empty();
    }
}