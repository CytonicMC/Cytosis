package net.cytonic.protocol.impl.objects.instances;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.instances.UpdateInstancesProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.InstanceResponse;

public class UpdateInstancesProtocolObject extends ProtocolObject<Packet, InstanceResponse> {

    @Override
    public String getSubject() {
        return "servers.update";
    }

    public record Packet(String instanceType) implements Message<Packet, InstanceResponse> {

    }
}
