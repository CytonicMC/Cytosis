package net.cytonic.protocol.impl.notify;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.notify.NotifyPacket;

public class CommandDisableNotifyPacket extends NotifyPacket<CommandDisableNotifyPacket.Packet> {

    @Override
    public String getSubject() {
        return "cytosis.commands.disabled";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public record Packet(String command, boolean enable) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "command", Codec.STRING, Packet::command,
            "enable", Codec.BOOLEAN, Packet::enable,
            Packet::new
        );
    }
}
