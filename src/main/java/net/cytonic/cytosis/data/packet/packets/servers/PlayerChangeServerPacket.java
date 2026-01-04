package net.cytonic.cytosis.data.packet.packets.servers;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.packets.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.packets.Packet;
import net.cytonic.cytosis.data.packet.packets.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class PlayerChangeServerPacket extends Packet<PlayerChangeServerPacket> {

    private final UUID player;
    private final String oldServer;
    private final String newServer;

    @Override
    protected Serializer<PlayerChangeServerPacket> getSerializer() {
        return new DefaultGsonSerializer<>(PlayerChangeServerPacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.PLAYER_SERVER_CHANGE;
    }
}
