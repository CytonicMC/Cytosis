package net.cytonic.protocol.notifyPackets;

import java.util.UUID;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.notifyPackets.PlayerRankUpdateNotifyPacket.Packet;

public class PlayerRankUpdateNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "players.rank.update";
    }

    public record Packet(UUID player, String rank) implements Message<Packet, Void> {

    }
}
