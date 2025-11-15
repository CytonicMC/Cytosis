package net.cytonic.cytosis.npcs;

import net.cytonic.cytosis.data.enums.NpcInteractType;
import net.cytonic.cytosis.player.CytosisPlayer;

/**
 * A functional interface for NPC actions
 */
@FunctionalInterface
public interface NpcAction {

    /**
     * A method to execute an NPC's action
     *
     * @param npc    The npc in question
     * @param type   the type of interaction
     * @param player the player who interacted
     */
    void execute(Npc npc, NpcInteractType type, CytosisPlayer player);
}
