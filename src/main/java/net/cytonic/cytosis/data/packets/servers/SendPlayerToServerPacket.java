package net.cytonic.cytosis.data.packets.servers;

import java.util.UUID;

import net.cytonic.cytosis.data.packets.Packet;

public record SendPlayerToServerPacket(UUID player, String serverId, UUID instance) implements Packet {

}
