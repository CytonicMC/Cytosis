package net.cytonic.cytosis.npcs;

import net.cytonic.cytosis.data.enums.NPCInteractType;
import net.minestom.server.entity.Player;

@FunctionalInterface
public interface NPCAction {
    void execute(NPC NPC, NPCInteractType type, Player player);
}
