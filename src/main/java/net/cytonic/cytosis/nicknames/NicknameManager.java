package net.cytonic.cytosis.nicknames;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.Tuple;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NicknameManager {
    private final Map<UUID, NicknameData> nicknames = new ConcurrentHashMap<>();

    public boolean isNicked(UUID player) {
        return nicknames.containsKey(player);
    }

    public void nicknamePlayer(UUID playerUuid) {
        if (Cytosis.getPlayer(playerUuid).isEmpty()) return;
        if (nicknames.containsKey(playerUuid)) return;

        CytosisPlayer player = Cytosis.getPlayer(playerUuid).get();

        nicknames.put(playerUuid, new NicknameData(
                NicknameGenerator.generateUsername(),
                NicknameGenerator.generateRank(),
                NicknameGenerator.generateSkin())
        );
        sendNicknamePacketstoAll(player, false);
    }

    /**
     * Sends the packets to the player to display their nickname.
     *
     * @param player      the player to "nickname"
     * @param freshViewer if the player has no existing viewer
     */
    public void sendNicknamePacketstoAll(CytosisPlayer player, boolean freshViewer) {
        for (Player viewer : player.getViewers()) {
            sendNicknamePacketsToPlayer(player, (CytosisPlayer) viewer, freshViewer);
        }
    }

    public void sendNicknamePacketsToPlayer(CytosisPlayer player, CytosisPlayer target, boolean freshViewer) {
        var properties = new ArrayList<PlayerInfoUpdatePacket.Property>();
        NicknameData data = getData(player.getUuid());
        if (data == null) return;
        if (data.signature() != null && data.value() != null) {
            properties.add(new PlayerInfoUpdatePacket.Property("textures", data.value(), data.signature()));
        }
        var entry = new PlayerInfoUpdatePacket.Entry(player.getUuid(), data.nickname(), properties, false,
                0, GameMode.SURVIVAL, null, null, 1);

        if (!freshViewer) {
            // remove the old player info and entity -- avoids nickname detection
            target.sendPackets(
                    new PlayerInfoRemovePacket(player.getUuid()),
                    new DestroyEntitiesPacket(player.getEntityId())
            );
        }

        target.sendPackets(
                new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry),
                new SpawnEntityPacket(player.getEntityId(), player.getUuid(), EntityType.PLAYER.id(), player.getPosition(), player.getPosition().yaw(), 0, (short) 0, (short) 0, (short) 0),
                new EntityMetaDataPacket(player.getEntityId(), Map.of(17, Metadata.Byte((byte) 127)))
        );
        Cytosis.getRankManager().setupCosmetics(player, data.rank());
    }

    @Nullable
    public NicknameData getData(UUID playerUuid) {
        return nicknames.get(playerUuid);
    }

    public record NicknameData(String nickname, PlayerRank rank, @Nullable String value, @Nullable String signature) {

        public NicknameData(String nickname, PlayerRank rank, Tuple<String, String> data) {
            this(nickname, rank, data.getSecond(), data.getFirst());
        }

        public static NicknameData parseBytes(byte[] serialized) {
            return Cytosis.GSON.fromJson(new String(serialized), NicknameData.class);
        }

        public static NicknameData parseString(String s) {
            return Cytosis.GSON.fromJson(s, NicknameData.class);
        }

        public byte[] serialize() {
            return Cytosis.GSON.toJson(this).getBytes();
        }

        public String serializeAsString() {
            return Cytosis.GSON.toJson(this);
        }
    }
}
