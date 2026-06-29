package net.cytonic.protocol.impl.notify;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.impl.notify.SnooperNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;
import net.cytonic.protocol.utils.ProtocolCodecUtils;

public class SnooperNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException("SnooperNotifyPacket does not have a default subject!");
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public record Packet(Component message) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "message", ProtocolCodecUtils.MINI_MESSAGE, Packet::message,
            Packet::new
        );
    }
}
