package net.cytonic.cytosis.data.containers;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.PlayerRank;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record PlayerRankUpdateContainer(UUID player, PlayerRank rank) {

    public static PlayerRankUpdateContainer deserialize(byte[] json) {
        return Cytosis.GSON.fromJson(new String(json), PlayerRankUpdateContainer.class);
    }

    public byte[] serialize() {
        return toString().getBytes();
    }

    @Override
    public @NotNull String toString() {
        return Cytosis.GSON.toJson(this);
    }
}
