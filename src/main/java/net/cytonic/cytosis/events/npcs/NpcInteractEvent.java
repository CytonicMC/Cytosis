package net.cytonic.cytosis.events.npcs;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minestom.server.event.trait.CancellableEvent;

import net.cytonic.cytosis.npcs.Npc;
import net.cytonic.cytosis.npcs.NpcAction;
import net.cytonic.cytosis.player.CytosisPlayer;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@Getter
public class NpcInteractEvent implements CancellableEvent {

    private final Npc npc;
    private final CytosisPlayer player;
    private final List<NpcAction> actions;
    @Setter
    private boolean cancelled;
}
