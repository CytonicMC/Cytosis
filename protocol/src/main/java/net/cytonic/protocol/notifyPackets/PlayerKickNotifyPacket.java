package net.cytonic.protocol.notifyPackets;

import java.util.UUID;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.data.enums.KickReason;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.notifyPackets.PlayerKickNotifyPacket.Packet;

@Internal
public class PlayerKickNotifyPacket extends NoResponse<Packet> {

    @Override
    public String getSubject() {
        return "players.kick";
    }

    @Internal
    public record Packet(UUID uuid, KickReason reason, JsonComponent message) implements Message<Packet, Void> {

    }
}
