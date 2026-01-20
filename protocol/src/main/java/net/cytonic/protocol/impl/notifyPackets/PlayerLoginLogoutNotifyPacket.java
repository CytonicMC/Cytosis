package net.cytonic.protocol.impl.notifyPackets;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.impl.notifyPackets.PlayerLoginLogoutNotifyPacket.Packet;

@NoArgsConstructor
@AllArgsConstructor
public class PlayerLoginLogoutNotifyPacket extends NoResponse<Packet> {

    private boolean isLogin;

    @Override
    public String getSubject() {
        return isLogin ? "players.connect" : "players.disconnect";
    }

    public record Packet(String username, UUID uuid) implements Message<Packet, Void> {

    }
}
