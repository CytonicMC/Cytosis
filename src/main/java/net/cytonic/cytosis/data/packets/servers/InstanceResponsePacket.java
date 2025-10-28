package net.cytonic.cytosis.data.packets.servers;

import net.cytonic.cytosis.data.packets.Packet;

public record InstanceResponsePacket(boolean success, String message) implements Packet {
}
