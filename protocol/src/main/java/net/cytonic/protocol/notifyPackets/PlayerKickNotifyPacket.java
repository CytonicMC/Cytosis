package net.cytonic.protocol.notifyPackets;

import java.util.UUID;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.data.enums.KickReason;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.notifyPackets.PlayerKickNotifyPacket.Packet;

public class PlayerKickNotifyPacket extends NoResponse<Packet> {

    @Override
    public String getSubject() {
        return "players.kick";
    }

    public record Packet(UUID uuid, KickReason reason, JsonComponent message) implements Message<Packet, Void> {

    }
}
