package net.cytonic.protocol.objects.instances;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.instances.DeleteInstanceProtocolObject.Packet;
import net.cytonic.protocol.responses.InstanceResponse;

public class DeleteInstanceProtocolObject extends ProtocolObject<Packet, InstanceResponse> {

    @Override
    public String getSubject() {
        return "servers.delete";
    }

    public record Packet(String instanceType, String allocId) implements Message<Packet, InstanceResponse> {

    }
}
