package net.cytonic.cytosis.events.network;

import java.util.UUID;

import net.minestom.server.event.Event;

public record PlayerJoinNetworkEvent(UUID player, String username) implements Event {

}
