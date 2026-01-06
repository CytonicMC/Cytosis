package net.cytonic.cytosis.data.packet.packets.friends;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.ReusablePacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@AllArgsConstructor
public class FriendApiResponsePacket extends ReusablePacket<FriendApiResponsePacket> {

    private final boolean success;
    private final String code;
    private final String message;

    @Override
    protected Serializer<FriendApiResponsePacket> getSerializer() {
        return new DefaultGsonSerializer<>(FriendApiResponsePacket.class);
    }
}
