package net.cytonic.cytosis.parties.packets;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import net.cytonic.cytosis.data.packets.Packet;

public record PartyLeavePacket(@SerializedName("player_id") UUID player) implements Packet {

}
