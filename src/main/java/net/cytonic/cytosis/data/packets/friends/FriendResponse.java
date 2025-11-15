package net.cytonic.cytosis.data.packets.friends;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import net.cytonic.cytosis.data.packets.Packet;

public record FriendResponse(@SerializedName("request_id") UUID requestId) implements Packet {

}
