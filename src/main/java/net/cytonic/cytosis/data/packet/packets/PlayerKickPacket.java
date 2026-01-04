package net.cytonic.cytosis.data.packet.packets;

import java.util.UUID;

import lombok.Getter;

import net.cytonic.cytosis.data.enums.KickReason;
import net.cytonic.cytosis.data.objects.JsonComponent;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
public class PlayerKickPacket extends Packet<PlayerKickPacket> {

    private final UUID uuid;
    private final KickReason reason;
    private final JsonComponent message;

    /**
     * The packet for when a player gets kicked
     *
     * @param uuid    the player's uuid
     * @param reason  the reason for the kick
     * @param message the message to send to the player when they are kicked
     */
    public PlayerKickPacket(UUID uuid, KickReason reason, JsonComponent message) {
        this.uuid = uuid;
        this.reason = reason;
        this.message = message;
    }

    @Override
    protected Serializer<PlayerKickPacket> getSerializer() {
        return new DefaultGsonSerializer<>(PlayerKickPacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.PLAYER_KICK;
    }
}
