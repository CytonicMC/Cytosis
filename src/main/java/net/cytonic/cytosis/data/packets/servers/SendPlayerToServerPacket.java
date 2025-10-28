package net.cytonic.cytosis.data.packets.servers;

import net.cytonic.cytosis.data.packets.Packet;

import java.util.UUID;

public record SendPlayerToServerPacket(UUID player, String serverId, UUID instance) implements Packet {
}
