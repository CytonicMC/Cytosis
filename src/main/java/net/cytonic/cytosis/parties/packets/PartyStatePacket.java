package net.cytonic.cytosis.parties.packets;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import net.cytonic.cytosis.data.packets.Packet;

public record PartyStatePacket(
    @SerializedName("party_id") UUID party,
    @SerializedName("player_id") UUID player,
    @SerializedName("state") boolean state) implements Packet {

}
