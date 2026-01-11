package net.cytonic.protocol.objects;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.HealthCheckProtocolObject.Packet;

public class HealthCheckProtocolObject extends NoResponse<Packet> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException();
    }

    public record Packet() implements Message<Packet, Void> {

        @Override
        public ProtocolObject<Packet, Void> getProtocolObject() {
            return new HealthCheckProtocolObject();
        }
    }
}
