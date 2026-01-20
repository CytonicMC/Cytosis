package net.cytonic.protocol.notifyPackets;

import java.util.UUID;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.notifyPackets.PlayerChangeServerNotifyPacket.Packet;

@Internal
public class PlayerChangeServerNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "players.server_change.notify";
    }

    @Internal
    public record Packet(UUID player, String oldServer, String newServer) implements Message<Packet, Void> {

    }
}
