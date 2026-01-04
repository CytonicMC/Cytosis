package net.cytonic.cytosis.parties.packets;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.data.packets.Packet;

public record PartyInviteSendPacket(
    @SerializedName("party_id") @Nullable UUID party,
    @SerializedName("sender_id") UUID sender,
    @SerializedName("recipient_id") UUID recipient) implements Packet {

}
