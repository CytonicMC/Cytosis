package net.cytonic.protocol.impl.objects;

import java.util.UUID;

import net.kyori.adventure.key.Key;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.SendPlayerToServerTypeProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.SendPlayerResponse;

public class SendPlayerToServerTypeProtocolObject extends ProtocolObject<Packet, SendPlayerResponse> {

    @Override
    public String getSubject() {
        return "players.send.generic";
    }

    public record Packet(UUID player, Key type) implements Message<Packet, SendPlayerResponse> {

    }
}
