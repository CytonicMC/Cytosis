package net.cytonic.cytosis.data.packets.servers;

import net.cytonic.cytosis.data.packets.Packet;

public record DeleteInstancePacket(String instanceType, String allocId) implements Packet {
}
