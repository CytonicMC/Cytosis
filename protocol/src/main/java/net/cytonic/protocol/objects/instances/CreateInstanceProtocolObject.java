package net.cytonic.protocol.objects.instances;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.instances.CreateInstanceProtocolObject.Packet;
import net.cytonic.protocol.responses.InstanceResponse;

@Internal
public class CreateInstanceProtocolObject extends ProtocolObject<Packet, InstanceResponse> {

    @Override
    public String getSubject() {
        return "servers.create";
    }

    @Internal
    public record Packet(String instanceType, int quantity) implements Message<Packet, InstanceResponse> {

    }
}
