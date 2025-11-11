package net.cytonic.cytosis.data.packets;

import java.util.UUID;

import net.cytonic.cytosis.data.enums.PlayerRank;

public record PlayerRankUpdatePacket(UUID player, PlayerRank rank) implements Packet {

}