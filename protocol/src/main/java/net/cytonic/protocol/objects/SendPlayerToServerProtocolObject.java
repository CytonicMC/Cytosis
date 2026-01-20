package net.cytonic.protocol.objects;

import java.util.UUID;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.SendPlayerToServerProtocolObject.Packet;
import net.cytonic.protocol.responses.SendPlayerResponse;

@Internal
public class SendPlayerToServerProtocolObject extends ProtocolObject<Packet, SendPlayerResponse> {

    @Override
    public String getSubject() {
        return "players.send";
    }

    @Internal
    public record Packet(UUID player, String serverId) implements Message<Packet, SendPlayerResponse> {

    }
}
