package net.cytonic.protocol.impl.notify;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.impl.notify.PlayerLoginLogoutNotifyPacket.Packet;

@NoArgsConstructor
@AllArgsConstructor
public class PlayerLoginLogoutNotifyPacket extends NoResponse<Packet> {

    private boolean isLogin;

    @Override
    public String getSubject() {
        return isLogin ? "players.connect" : "players.disconnect";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public record Packet(String username, UUID uuid) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "username", Codec.STRING, Packet::username,
            "uuid", Codec.UUID_STRING, Packet::uuid,
            Packet::new
        );
    }
}
