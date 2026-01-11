package net.cytonic.protocol.notifyPackets;

import java.util.UUID;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.notifyPackets.PlayerChangeServerNotifyPacket.Packet;

public class PlayerChangeServerNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "players.server_change.notify";
    }

    public record Packet(UUID player, String oldServer, String newServer) implements Message<Packet, Void> {

        @Override
        public ProtocolObject<Packet, Void> getProtocolObject() {
            return new PlayerChangeServerNotifyPacket();
        }
    }

}
