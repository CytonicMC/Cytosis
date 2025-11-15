package net.cytonic.cytosis.data.packets.servers;

import net.cytonic.cytosis.data.packets.Packet;

public record DeleteAllInstancesPacket(String instanceType) implements Packet {
}
