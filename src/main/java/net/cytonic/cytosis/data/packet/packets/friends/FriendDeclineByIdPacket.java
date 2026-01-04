package net.cytonic.cytosis.data.packet.packets.friends;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.packets.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.packets.RequestPacket;
import net.cytonic.cytosis.data.packet.packets.Serializer;

@Getter
@AllArgsConstructor
public class FriendDeclineByIdPacket extends RequestPacket<FriendDeclineByIdPacket, FriendApiResponsePacket> {

    @SerializedName("request_id")
    private final UUID requestId;

    @Override
    protected Serializer<FriendDeclineByIdPacket> getSerializer() {
        return new DefaultGsonSerializer<>(FriendDeclineByIdPacket.class);
    }

    @Override
    protected Class<FriendApiResponsePacket> getResponseType() {
        return FriendApiResponsePacket.class;
    }
}
