package net.cytonic.protocol.impl.objects;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.AccountLinkProtocolObject.Packet;
import net.cytonic.protocol.impl.objects.AccountLinkProtocolObject.Response;

public class AccountLinkProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public String getSubject() {
        return "player.link";
    }

    public record Packet(UUID uuid) implements Message<Packet, Response> {

    }

    public record Response(String token, @Nullable String error) {

    }
}
