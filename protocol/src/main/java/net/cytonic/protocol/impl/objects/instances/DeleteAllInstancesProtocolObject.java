package net.cytonic.protocol.impl.objects.instances;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.instances.DeleteAllInstancesProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.InstanceResponse;

public class DeleteAllInstancesProtocolObject extends ProtocolObject<Packet, InstanceResponse> {

    @Override
    public String getSubject() {
        return "servers.delete.all";
    }

    public record Packet(String instanceType) implements Message<Packet, InstanceResponse> {

    }
}
