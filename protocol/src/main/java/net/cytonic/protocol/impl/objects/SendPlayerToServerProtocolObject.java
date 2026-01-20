package net.cytonic.protocol.impl.objects;

import java.util.UUID;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.SendPlayerToServerProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.SendPlayerResponse;

public class SendPlayerToServerProtocolObject extends ProtocolObject<Packet, SendPlayerResponse> {

    @Override
    public String getSubject() {
        return "players.send";
    }

    public record Packet(UUID player, String serverId) implements Message<Packet, SendPlayerResponse> {

    }
}
