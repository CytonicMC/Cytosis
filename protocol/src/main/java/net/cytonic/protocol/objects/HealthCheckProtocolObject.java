package net.cytonic.protocol.objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.HealthCheckProtocolObject.Packet;
import net.cytonic.protocol.objects.HealthCheckProtocolObject.Response;

@Internal
public class HealthCheckProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException("HealthCheckProtocolObject does not have a default subject!");
    }

    @Internal
    public record Packet() implements Message<Packet, Response> {

    }

    @Internal
    public record Response() {

    }
}
