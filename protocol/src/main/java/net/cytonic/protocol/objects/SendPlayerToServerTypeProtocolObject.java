package net.cytonic.protocol.objects;

import java.util.UUID;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.SendPlayerToServerTypeProtocolObject.Packet;
import net.cytonic.protocol.responses.SendPlayerResponse;

public class SendPlayerToServerTypeProtocolObject extends ProtocolObject<Packet, SendPlayerResponse> {

    @Override
    public String getSubject() {
        return "players.send.generic";
    }

    public record Packet(UUID player, String group, String type) implements Message<Packet, SendPlayerResponse> {

    }
}
