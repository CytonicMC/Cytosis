package net.cytonic.cytosis.events.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;

import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.player.CytosisPlayer;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerSendMessageEvent implements CancellableEvent, InstanceEvent, PlayerEvent {

    private final Instance instance;
    private final CytosisPlayer player;
    private final Component message;
    private final ChatChannel channel;
    private boolean cancelled = false;
}
