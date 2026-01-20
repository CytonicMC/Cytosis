package net.cytonic.protocol.impl.notifyPackets;

import java.util.UUID;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.impl.notifyPackets.PlayerRankUpdateNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class PlayerRankUpdateNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "players.rank.update";
    }

    public record Packet(UUID player, String rank) implements Message<Packet, Void> {

    }
}
