package net.cytonic.cytosis.data.packet.packets.friends;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.ReusableRequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendPacket extends ReusableRequestPacket<FriendPacket, FriendApiResponsePacket> {

    private UUID sender;
    private UUID recipient;

    @Override
    protected Serializer<FriendPacket> getSerializer() {
        return new DefaultGsonSerializer<>(FriendPacket.class);
    }

    @Override
    protected Class<FriendApiResponsePacket> getResponseType() {
        return FriendApiResponsePacket.class;
    }
}
