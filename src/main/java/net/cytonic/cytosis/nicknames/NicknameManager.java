package net.cytonic.cytosis.nicknames;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.Tuple;
import net.cytonic.cytosis.events.Events;
import net.cytonic.cytosis.managers.PlayerListManager;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.MetadataPacketBuilder;
import net.cytonic.cytosis.utils.Msg;

@CytosisComponent(dependsOn = {PlayerListManager.class})
public class NicknameManager implements Bootstrappable {

    //todo: Look into masking UUIDs in outgoing packets
    private final Map<UUID, NicknameData> nicknames = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> maskedUuids = new ConcurrentHashMap<>();
    private RankManager rankManager;
    private RedisDatabase redis;
    private MysqlDatabase db;

    public NicknameManager() {
        // remove them from this memory cache
        Events.onNetworkLeave(event -> {
            nicknames.remove(event.player());
            maskedUuids.remove(event.player());
        });
    }

    public static String translateSkin(CytosisPlayer player, String skin) {
        if (skin == null) {
            return "<#BE9025>Steve/Alex skin</#BE9025>";
        }
        if (skin.equals(player.getSkin().textures())) {
            return "<#BE9025>My normal skin</#BE9025>";
        }
        return "<#BE9025>Random Skin</#BE9025>";
    }

    @Override
    public void init() {
        this.rankManager = Cytosis.get(RankManager.class);
        this.db = Cytosis.get(MysqlDatabase.class);
        this.redis = Cytosis.get(RedisDatabase.class);
    }

    public boolean isNicked(UUID player) {
        return nicknames.containsKey(player);
    }

    public void nicknamePlayer(UUID playerUuid, NicknameData data) {
        if (Cytosis.getPlayer(playerUuid).isEmpty()) return;
        if (nicknames.containsKey(playerUuid)) return;

        CytosisPlayer player = Cytosis.getPlayer(playerUuid).get();
        UUID masked = maskedUuids.computeIfAbsent(playerUuid, uuid -> UUID.randomUUID());

        nicknames.put(playerUuid, data);
        addToTrackedNicknames(playerUuid, data.nickname());
        sendNicknamePacketstoAll(player, masked, false);
        player.updatePreference(CytosisNamespaces.NICKNAME_DATA, data);
        player.updatePreference(CytosisNamespaces.NICKED_UUID, masked);

        Component msg = player.trueFormattedName().append(Msg.aqua(" has been nicked to "))
            .append(player.formattedName())
            .append(
                Msg.aqua(" (Skin: %s)!", Msg.stripTags(translateSkin(player, data.value()).replace("My", "Their"))));

        Cytosis.get(SnooperManager.class).sendSnoop(CytosisSnoops.PLAYER_NICKNAME, Msg.snoop(msg));
    }

    /**
     * Sends the packets to the player to display their nickname.
     *
     * @param player      the player to "nickname"
     * @param freshViewer if the player has no existing viewer
     */
    public void sendNicknamePacketstoAll(CytosisPlayer player, UUID masked, boolean freshViewer) {
        for (Player viewer : player.getViewers()) {
            sendNicknamePacketsToPlayer(player, (CytosisPlayer) viewer, masked, freshViewer);
        }
    }

    public void sendNicknamePacketsToPlayer(CytosisPlayer player, CytosisPlayer target, UUID masked,
        boolean freshViewer) {
        ArrayList<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();
        NicknameData data = getData(player.getUuid());
        if (data == null) return;
        if (data.signature() != null && data.value() != null) {
            properties.add(new PlayerInfoUpdatePacket.Property("textures", data.value(), data.signature()));
        }
        PlayerInfoUpdatePacket.Entry entry = new PlayerInfoUpdatePacket.Entry(masked, data.nickname(), properties,
            false, 0, GameMode.SURVIVAL, null, null, 1, true);

        if (!freshViewer) {
            // remove the old player info and entity -- avoids nickname detection
            target.sendPackets(new PlayerInfoRemovePacket(player.getUuid()), // look into this
                new DestroyEntitiesPacket(player.getEntityId()));
        }

        target.sendPackets(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry),
            new SpawnEntityPacket(player.getEntityId(), masked, EntityType.PLAYER, player.getPosition(),
                player.getPosition().yaw(), 0, Vec.ZERO),
            MetadataPacketBuilder.builder(player.getMetadataPacket())
                .setByte(17, (byte) 127)
                .build());
        rankManager.setupCosmetics(player, data.rank());
    }

    @Nullable
    public NicknameData getData(UUID playerUuid) {
        return nicknames.get(playerUuid);
    }

    public void disableNickname(UUID playerUuid) {
        if (Cytosis.getPlayer(playerUuid).isEmpty()) return;
        if (!nicknames.containsKey(playerUuid)) return;

        CytosisPlayer player = Cytosis.getPlayer(playerUuid).get();

        NicknameData data = nicknames.remove(playerUuid);
        if (data == null) return;
        redis.removeFromHash("cytosis:nicknames", playerUuid.toString());
        sendRemovePackets(player);
        rankManager.setupCosmetics(player, player.getTrueRank());
        player.updatePreference(CytosisNamespaces.NICKNAME_DATA, null);
        player.updatePreference(CytosisNamespaces.NICKED_UUID, null);
    }

    public void sendRemovePackets(CytosisPlayer player) {
        final UUID masked = maskedUuids.get(player.getUuid());
        for (Player viewer : player.getViewers()) {
            ArrayList<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();

            if (player.getTrueSkin() == null) return;
            if (player.getTrueSkin().signature() != null && player.getTrueSkin().textures() != null) {
                properties.add(new PlayerInfoUpdatePacket.Property("textures", player.getTrueSkin()
                    .textures(), player.getTrueSkin()
                    .signature()));
            }
            PlayerInfoUpdatePacket.Entry entry = new PlayerInfoUpdatePacket.Entry(player.getUuid(),
                player.getTrueUsername(), properties, false, 0, GameMode.SURVIVAL, null, null, 1, true);
            // remove the old player info and entity
            viewer.sendPackets(new PlayerInfoRemovePacket(masked), new DestroyEntitiesPacket(player.getEntityId()));
            viewer.sendPackets(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry),
                new SpawnEntityPacket(player.getEntityId(), player.getUuid(), EntityType.PLAYER,
                    player.getPosition(), player.getPosition()
                    .yaw(), 0, Vec.ZERO),
                MetadataPacketBuilder.builder(player.getMetadataPacket())
                    .setByte(17, (byte) 127)
                    .build());
        }
    }

    public void loadNickedPlayer(CytosisPlayer player) {
        NicknameData data = Cytosis.get(PreferenceManager.class)
            .getPlayerPreference(player.getUuid(), CytosisNamespaces.NICKNAME_DATA);
        UUID maskedUuid = player.getPreference(CytosisNamespaces.NICKED_UUID);
        if (maskedUuid == null) {
            maskedUuid = UUID.randomUUID();
        }
        if (data == null) return;
        this.maskedUuids.put(player.getUuid(), maskedUuid);
        this.nicknames.put(player.getUuid(), data);
        addToTrackedNicknames(player.getUuid(), data.nickname());
        sendNicknamePacketstoAll(player, maskedUuid, false);
    }

    @Nullable
    public CytosisPlayer getPlayerByNickname(String name) {
        for (Map.Entry<UUID, NicknameData> entry : nicknames.entrySet()) {
            if (entry.getValue().nickname().equalsIgnoreCase(name)) {
                return Cytosis.getPlayer(entry.getKey()).orElse(null);
            }
        }
        return null;
    }

    public List<String> getActiveNicknames() {
        List<String> names = new ArrayList<>();
        for (Map.Entry<UUID, NicknameData> entry : nicknames.entrySet()) {
            names.add(entry.getValue().nickname());
        }
        return names;
    }

    public Set<String> getNetworkNicknames() {
        return new HashSet<>(redis.getHash("cytosis:nicknames").values());
    }

    private void addToTrackedNicknames(UUID playerUuid, String nickname) {
        redis.addToHash("cytosis:nicknames", playerUuid.toString(), nickname);
        redis.addToHash("cytosis:nicknames_reverse", nickname, playerUuid.toString());
    }

    public @Nullable UUID deanonymizePlayer(String nickname) {
        // on this server
        for (Map.Entry<UUID, NicknameData> entry : nicknames.entrySet()) {
            if (entry.getValue().nickname().equalsIgnoreCase(nickname)) {
                return entry.getKey();
            }
        }

        String raw = redis.getFromHash("cytosis:nicknames_reverse", nickname);
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public record NicknameData(String nickname, PlayerRank rank, @Nullable String value, @Nullable String signature) {

        public static final NicknameData EMPTY = new NicknameData("", PlayerRank.DEFAULT, null, null);

        public NicknameData(String nickname, PlayerRank rank, Tuple<String, String> skin) {
            this(nickname, rank, skin.getSecond(), skin.getFirst());

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

        public NicknameData withRank(PlayerRank rank) {
            return new NicknameData(nickname(), rank, value(), signature());
        }

        public NicknameData withSkin(Tuple<String, String> skin) {
            return new NicknameData(nickname(), rank(), skin.getSecond(), skin.getFirst());
        }

        public NicknameData withSkin(String signature, String value) {
            return new NicknameData(nickname(), rank(), value, signature);
        }

        public NicknameData withNickname(String nickname) {
            return new NicknameData(nickname, rank(), value(), signature());
        }
    }
}