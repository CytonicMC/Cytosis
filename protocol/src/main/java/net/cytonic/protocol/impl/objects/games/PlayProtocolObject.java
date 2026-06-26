package net.cytonic.protocol.impl.objects.games;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.games.PlayProtocolObject.Packet;
import net.cytonic.protocol.impl.objects.games.PlayProtocolObject.Response;


@NoArgsConstructor
@AllArgsConstructor
public class PlayProtocolObject extends ProtocolObject<Packet, Response> {

    private String subject;

    @Override
    public String getSubject() {
        return subject;
    }

    public record Packet(UUID player) implements Message<Packet, Response> {

    }

    public record Response(Key serverType, @Nullable String error) {

    }
}
