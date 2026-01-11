package net.cytonic.protocol.data.objects;

import java.time.Instant;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

public record PartyInvite(
    @SerializedName("id") UUID id,
    @SerializedName("party_id") UUID partyId,
    @SerializedName("recipient") UUID recipient,
    @SerializedName("sender_id") UUID sender,
    @SerializedName("expiry") Instant expiry
) {

}
