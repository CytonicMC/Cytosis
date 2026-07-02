package net.cytonic.protocol.impl.objects.instances;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.instances.DeleteAllInstancesProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.GenericResponse;

public class DeleteAllInstancesProtocolObject extends ProtocolObject<Packet, GenericResponse> {

    @Override
    public String getSubject() {
        return "servers.delete.all";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    @Override
    public Codec<GenericResponse> getReturnCodec() {
        return GenericResponse.CODEC;
    }

    public record Packet(String instanceType) implements Message<Packet, GenericResponse> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "instanceType", Codec.STRING, Packet::instanceType,
            Packet::new
        );
    }
}
