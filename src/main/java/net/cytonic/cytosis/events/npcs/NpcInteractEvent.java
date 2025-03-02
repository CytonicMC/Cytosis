package net.cytonic.cytosis.events.npcs;

import net.cytonic.cytosis.npcs.NPC;
import net.cytonic.cytosis.npcs.NPCAction;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.event.Event;

import java.util.List;


@SuppressWarnings("unused")
public record NpcInteractEvent(NPC npc, CytosisPlayer player, List<NPCAction> actions) implements Event {
}
