package net.cytonic.protocol.objects.instances;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.instances.DeleteAllInstancesProtocolObject.Packet;
import net.cytonic.protocol.responses.InstanceResponse;

public class DeleteAllInstancesProtocolObject extends ProtocolObject<Packet, InstanceResponse> {

    @Override
    public String getSubject() {
        return "servers.delete.all";
    }

    public record Packet(String instanceType) implements Message<Packet, InstanceResponse> {

    }
}
