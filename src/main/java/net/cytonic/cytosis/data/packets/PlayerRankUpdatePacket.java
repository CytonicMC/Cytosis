package net.cytonic.cytosis.data.packets;

import net.cytonic.cytosis.data.enums.PlayerRank;

import java.util.UUID;

public record PlayerRankUpdatePacket(UUID player, PlayerRank rank) implements Packet {
}