package net.cytonic.cytosis.data.packets.servers;

import net.cytonic.cytosis.data.packets.Packet;

public record CreateInstancePacket(String instanceType, int quantity) implements Packet {
}
