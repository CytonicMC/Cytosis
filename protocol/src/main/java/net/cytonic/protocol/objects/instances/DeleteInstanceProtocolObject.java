package net.cytonic.protocol.objects.instances;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.instances.DeleteInstanceProtocolObject.Packet;
import net.cytonic.protocol.responses.InstanceResponse;

@Internal
public class DeleteInstanceProtocolObject extends ProtocolObject<Packet, InstanceResponse> {

    @Override
    public String getSubject() {
        return "servers.delete";
    }

    @Internal
    public record Packet(String instanceType, String allocId) implements Message<Packet, InstanceResponse> {

    }
}
