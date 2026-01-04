package net.cytonic.cytosis.parties.packets;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import net.cytonic.cytosis.data.packets.Packet;

/*
type PartyInviteExpirePacket struct {
	RequestID uuid.UUID `json:"request_id"`
	PartyID   uuid.UUID `json:"party_id"`
	Recipient uuid.UUID `json:"recipient"`
	SenderID  uuid.UUID `json:"sender_id"`
}
 */
public record PartyInviteExpirePacket(
    @SerializedName("request_id") UUID request,
    @SerializedName("party_id") UUID party,
    @SerializedName("recipient") UUID recipient,
    @SerializedName("sender_id") UUID sender
) implements Packet {

}
