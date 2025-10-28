package net.cytonic.cytosis.data.packets.friends;

import com.google.gson.annotations.SerializedName;
import net.cytonic.cytosis.data.packets.Packet;

import java.util.UUID;

public record FriendResponse(@SerializedName("request_id") UUID requestId) implements Packet {
}
