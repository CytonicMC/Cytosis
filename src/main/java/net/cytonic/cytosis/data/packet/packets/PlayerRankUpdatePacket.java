package net.cytonic.cytosis.data.packet.packets;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class PlayerRankUpdatePacket extends Packet<PlayerRankUpdatePacket> {

    private final UUID player;
    private final PlayerRank rank;

    @Override
    protected Serializer<PlayerRankUpdatePacket> getSerializer() {
        return new DefaultGsonSerializer<>(PlayerRankUpdatePacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.PLAYER_RANK_UPDATE;
    }
}