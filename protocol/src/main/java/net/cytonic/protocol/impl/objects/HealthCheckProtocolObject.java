package net.cytonic.protocol.impl.objects;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.HealthCheckProtocolObject.Packet;
import net.cytonic.protocol.impl.objects.HealthCheckProtocolObject.Response;

public class HealthCheckProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException("HealthCheckProtocolObject does not have a default subject!");
    }

    public record Packet() implements Message<Packet, Response> {

    }

    public record Response() {

    }
}
