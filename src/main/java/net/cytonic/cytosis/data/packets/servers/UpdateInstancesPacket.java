package net.cytonic.cytosis.data.packets.servers;

import net.cytonic.cytosis.data.packets.Packet;

public record UpdateInstancesPacket(String instanceType) implements Packet {
}
