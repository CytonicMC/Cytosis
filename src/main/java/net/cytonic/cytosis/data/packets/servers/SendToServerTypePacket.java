package net.cytonic.cytosis.data.packets.servers;

import net.cytonic.cytosis.data.packets.Packet;

import java.util.UUID;

public record SendToServerTypePacket(UUID player, String group, String type) implements Packet {
}
