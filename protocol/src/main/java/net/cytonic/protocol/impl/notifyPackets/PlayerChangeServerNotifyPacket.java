package net.cytonic.protocol.impl.notifyPackets;

import java.util.UUID;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.impl.notifyPackets.PlayerChangeServerNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class PlayerChangeServerNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "players.server_change.notify";
    }

    public record Packet(UUID player, String oldServer, String newServer) implements Message<Packet, Void> {

    }
}
