package net.cytonic.cytosis.npcs;

import net.cytonic.cytosis.data.enums.NPCInteractType;
import net.minestom.server.entity.Player;

/**
 * A functional interface for NPC actions
 */
@FunctionalInterface
public interface NPCAction {
    /**
     * A method to execute an NPC action
     *
     * @param NPC    The NPC in question
     * @param type   the type of interaction
     * @param player the player who interacted
     */
    void execute(NPC NPC, NPCInteractType type, Player player);
}
