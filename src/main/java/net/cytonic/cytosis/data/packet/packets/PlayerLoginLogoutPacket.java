package net.cytonic.cytosis.data.packet.packets;

import java.util.UUID;

import lombok.Getter;

import net.cytonic.cytosis.messaging.Subjects;

@Getter
public class PlayerLoginLogoutPacket extends Packet<PlayerLoginLogoutPacket> {

    private final String username;
    private final UUID uuid;
    private final transient boolean isLoggingIn;

    /**
     * The packet for when the player logs in or logs out
     *
     * @param username the username of the player
     * @param uuid     the uuid of the player
     */
    public PlayerLoginLogoutPacket(UUID uuid, String username, boolean isLoggingIn) {
        this.username = username;
        this.uuid = uuid;
        this.isLoggingIn = isLoggingIn;
    }

    @Override
    protected Serializer<PlayerLoginLogoutPacket> getSerializer() {
        return new DefaultGsonSerializer<>(PlayerLoginLogoutPacket.class);
    }

    @Override
    public String getSubject() {
        return isLoggingIn ? Subjects.PLAYER_JOIN : Subjects.PLAYER_LEAVE;
    }
}
