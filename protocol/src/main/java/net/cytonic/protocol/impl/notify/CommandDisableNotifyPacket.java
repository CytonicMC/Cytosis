package net.cytonic.protocol.impl.notify;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.notify.NotifyPacket;

public class CommandDisableNotifyPacket extends NotifyPacket<CommandDisableNotifyPacket.Packet> {

    @Override
    public String getSubject() {
        return "cytosis.commands.disabled";
    }

    public record Packet(String command, boolean enable) implements Message<Packet, Void> {

    }
}
