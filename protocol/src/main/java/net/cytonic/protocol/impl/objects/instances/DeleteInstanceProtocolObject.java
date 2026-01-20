package net.cytonic.protocol.impl.objects.instances;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.instances.DeleteInstanceProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.InstanceResponse;

public class DeleteInstanceProtocolObject extends ProtocolObject<Packet, InstanceResponse> {

    @Override
    public String getSubject() {
        return "servers.delete";
    }

    public record Packet(String instanceType, String allocId) implements Message<Packet, InstanceResponse> {

    }
}
