package net.cytonic.cytosis.data.packets.servers;

import net.cytonic.cytosis.data.packets.Packet;

import java.util.UUID;

public record PlayerChangeServerPacket(UUID player, String oldServer, String newServer) implements Packet {
}
