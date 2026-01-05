package net.cytonic.cytosis.data.packet.packets.friends;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendApiRequestPacket extends RequestPacket<FriendApiRequestPacket, FriendApiResponsePacket> {

    private UUID sender;
    private UUID recipient;
    private Instant expiry;

    @Override
    protected Serializer<FriendApiRequestPacket> getSerializer() {
        return new DefaultGsonSerializer<>(FriendApiRequestPacket.class, Cytosis.GO_GSON);
    }

    @Override
    protected Class<FriendApiResponsePacket> getResponseType() {
        return FriendApiResponsePacket.class;
    }

    @Override
    public String getSubject() {
        return Subjects.FRIEND_REQUEST;
    }
}
