package net.cytonic.cytosis.server.player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.minestomunited.entrypoint.player.PlayerData;
import dev.minestomunited.entrypoint.player.PlayerService;
import io.ebean.DB;
import org.jetbrains.annotations.Nullable;

public class PlayerServiceImpl implements PlayerService {

    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    @Override
    public void updatePlayerData(PlayerData playerData) {
        PlayerDataEntity playerDataEntity = new PlayerDataEntity();
        playerDataEntity.uuid(playerData.uuid());
        playerDataEntity.username(playerData.username());
        playerDataEntity.skinSignature(playerData.playerSkin().signature());
        playerDataEntity.skinTextures(playerData.playerSkin().textures());
        playerDataEntity.version(playerData.version());
        playerDataEntity.save();
        playerDataMap.put(playerData.uuid(), playerData);
    }

    @Override
    public void unloadPlayerData(UUID playerId) {
        PlayerData data = playerDataMap.get(playerId);
        if (data != null) {
            updatePlayerData(data);
        }
        playerDataMap.remove(playerId);
    }

    @Override
    public @Nullable PlayerData loadPlayerData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, k ->
            DB.find(PlayerDataEntity.class, k));
    }
}
