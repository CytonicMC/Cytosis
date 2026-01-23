package net.cytonic.cytosis.events.npcs;

import lombok.Getter;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jspecify.annotations.NonNull;

import net.cytonic.cytosis.data.enums.NpcInteractType;
import net.cytonic.cytosis.entity.npc.NPC;
import net.cytonic.cytosis.player.CytosisPlayer;

@Getter
public class NPCInteractEvent implements PlayerInstanceEvent, CancellableEvent {

    private final CytosisPlayer player;
    private final NpcInteractType interactType;
    @Getter
    private final NPC npc;
    private Boolean cancelled = false;

    public NPCInteractEvent(CytosisPlayer player, NpcInteractType interactType, NPC npc) {
        this.player = player;
        this.interactType = interactType;
        this.npc = npc;
    }

    @Override
    public @NonNull Player getPlayer() {
        return player;
    }

    public CytosisPlayer player() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
