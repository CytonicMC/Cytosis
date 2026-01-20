package net.cytonic.protocol.impl.objects;

import java.util.UUID;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.SendPlayerToServerTypeProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.SendPlayerResponse;

public class SendPlayerToServerTypeProtocolObject extends ProtocolObject<Packet, SendPlayerResponse> {

    @Override
    public String getSubject() {
        return "players.send.generic";
    }

    public record Packet(UUID player, String group, String type) implements Message<Packet, SendPlayerResponse> {

    }
}
