package net.cytonic.cytosis.data.packet.packets.friends;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.serializers.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@AllArgsConstructor
public class FriendAcceptByIdPacket extends RequestPacket<FriendAcceptByIdPacket, FriendApiResponsePacket> {

    @SerializedName("request_id")
    private final UUID requestId;

    @Override
    protected Serializer<FriendAcceptByIdPacket> getSerializer() {
        return new DefaultGsonSerializer<>(FriendAcceptByIdPacket.class);
    }

    @Override
    protected Class<FriendApiResponsePacket> getResponseType() {
        return FriendApiResponsePacket.class;
    }
}
