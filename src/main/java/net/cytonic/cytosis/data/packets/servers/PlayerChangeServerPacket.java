package net.cytonic.cytosis.data.packets.servers;

import java.util.UUID;

import net.cytonic.cytosis.data.packets.Packet;

public record PlayerChangeServerPacket(UUID player, String oldServer, String newServer) implements Packet {

}
