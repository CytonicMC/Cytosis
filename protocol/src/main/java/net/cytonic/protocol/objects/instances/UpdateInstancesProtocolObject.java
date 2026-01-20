package net.cytonic.protocol.objects.instances;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.instances.UpdateInstancesProtocolObject.Packet;
import net.cytonic.protocol.responses.InstanceResponse;

@Internal
public class UpdateInstancesProtocolObject extends ProtocolObject<Packet, InstanceResponse> {

    @Override
    public String getSubject() {
        return "servers.update";
    }

    @Internal
    public record Packet(String instanceType) implements Message<Packet, InstanceResponse> {

    }
}
