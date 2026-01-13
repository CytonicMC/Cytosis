package net.cytonic.protocol.objects;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.HealthCheckProtocolObject.Packet;
import net.cytonic.protocol.objects.HealthCheckProtocolObject.Response;

public class HealthCheckProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException();
    }

    public record Packet() implements Message<Packet, Response> {

    }

    public record Response() {

    }
}
