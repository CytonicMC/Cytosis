package net.cytonic.cytosis.parties.packets;

import net.cytonic.cytosis.data.packets.Packet;

public record PartyResponsePacket(boolean success, String message) implements Packet {

}
