package net.cytonic.cytosis.data.packet.packets.friends;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.IllegalSubjectException;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@AllArgsConstructor
public class FriendApiResponsePacket extends Packet<FriendApiResponsePacket> {

    private final boolean success;
    private final String code;
    private final String message;

    @Override
    protected Serializer<FriendApiResponsePacket> getSerializer() {
        return new DefaultGsonSerializer<>(FriendApiResponsePacket.class);
    }

    @Override
    public String getSubject() {
        throw new IllegalSubjectException();
    }
}
