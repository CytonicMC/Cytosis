package net.cytonic.protocol.notifyPackets;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.notifyPackets.PlayerLoginLogoutNotifyPacket.Packet;

@NoArgsConstructor
@AllArgsConstructor
@Internal
public class PlayerLoginLogoutNotifyPacket extends NoResponse<Packet> {

    private boolean isLogin;

    @Override
    public String getSubject() {
        return isLogin ? "players.connect" : "players.disconnect";
    }

    @Internal
    public record Packet(String username, UUID uuid) implements Message<Packet, Void> {

    }
}
