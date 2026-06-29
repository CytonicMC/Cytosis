package net.cytonic.protocol.data.objects;

import java.time.Instant;
import java.util.UUID;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.utils.ProtocolCodecUtils;

public record PartyInvite(
    UUID id,
    UUID partyId,
    UUID recipient,
    UUID sender,
    Instant expiry
) {

    public static final Codec<PartyInvite> CODEC = StructCodec.struct(
        "id", Codec.UUID_STRING, PartyInvite::id,
        "party_id", Codec.UUID_STRING, PartyInvite::partyId,
        "recipient", Codec.UUID_STRING, PartyInvite::recipient,
        "sender_id", Codec.UUID_STRING, PartyInvite::sender,
        "expiry", ProtocolCodecUtils.GO_INSTANT, PartyInvite::expiry,
        PartyInvite::new
    );
}
