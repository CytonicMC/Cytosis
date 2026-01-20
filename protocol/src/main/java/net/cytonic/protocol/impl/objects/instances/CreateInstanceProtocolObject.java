package net.cytonic.protocol.impl.objects.instances;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.instances.CreateInstanceProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.InstanceResponse;

public class CreateInstanceProtocolObject extends ProtocolObject<Packet, InstanceResponse> {

    @Override
    public String getSubject() {
        return "servers.create";
    }

    public record Packet(String instanceType, int quantity) implements Message<Packet, InstanceResponse> {

    }
}
