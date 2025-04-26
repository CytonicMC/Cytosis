package net.cytonic.cytosis.events.network;

import net.minestom.server.event.Event;

import java.util.UUID;

public record PlayerJoinNetworkEvent(UUID player, String username) implements Event {
}
