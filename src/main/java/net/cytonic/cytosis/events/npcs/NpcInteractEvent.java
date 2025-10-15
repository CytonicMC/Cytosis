package net.cytonic.cytosis.events.npcs;

import java.util.List;

import net.minestom.server.event.Event;

import net.cytonic.cytosis.npcs.Npc;
import net.cytonic.cytosis.npcs.NpcAction;
import net.cytonic.cytosis.player.CytosisPlayer;

@SuppressWarnings("unused")
public record NpcInteractEvent(Npc npc, CytosisPlayer player, List<NpcAction> actions) implements Event {

}
