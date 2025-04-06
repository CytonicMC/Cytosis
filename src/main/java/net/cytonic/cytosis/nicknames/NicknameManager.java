package net.cytonic.cytosis.nicknames;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.Tuple;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Metadata;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
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
        String nick = NicknameGenerator.generateUsername();
        Tuple<String, String> skin = NicknameGenerator.generateSkin();

        nicknames.put(playerUuid, new NicknameData(nick, skin.getSecond(), skin.getFirst()));

        var properties = new ArrayList<PlayerInfoUpdatePacket.Property>();

        if (skin.getSecond() != null && skin.getFirst() != null) {
            properties.add(new PlayerInfoUpdatePacket.Property("textures", skin.getFirst(), skin.getSecond()));
        }
        var entry = new PlayerInfoUpdatePacket.Entry(playerUuid, nick, properties, true,
                0, GameMode.SURVIVAL, null, null, -1);


        player.sendPacketsToViewers(
                new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry),
                new EntityMetaDataPacket(player.getEntityId(), Map.of(17, Metadata.Byte((byte) 127)))
        );
        player.updateViewerRule();
    }

    @Nullable
    public NicknameData getData(UUID playerUuid) {
        return nicknames.get(playerUuid);
    }

    public record NicknameData(String nickname, @Nullable String value, @Nullable String signature) {
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
