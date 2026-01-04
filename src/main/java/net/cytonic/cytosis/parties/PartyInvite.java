package net.cytonic.cytosis.parties;

import java.time.Instant;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

/*
type PartyInvite struct {
	ID        uuid.UUID `json:"id"`
	PartyID   uuid.UUID `json:"party_id"`
	Recipient uuid.UUID `json:"recipient"`
	SenderID  uuid.UUID `json:"sender_id"` // can be a moderator or anyone if OpenInvites is enabled
	Expiry    time.Time `json:"expiry"`
}
 */
public record PartyInvite(
    @SerializedName("id") UUID id,
    @SerializedName("party_id") UUID partyId,
    @SerializedName("recipient") UUID recipient,
    @SerializedName("sender_id") UUID sender,
    @SerializedName("expiry") Instant expiry
) {

}
