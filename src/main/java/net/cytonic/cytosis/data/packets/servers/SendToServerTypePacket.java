package net.cytonic.cytosis.data.packets.servers;

import java.util.UUID;

import net.cytonic.cytosis.data.packets.Packet;

public record SendToServerTypePacket(UUID player, String group, String type) implements Packet {

}
