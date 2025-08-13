package net.cytonic.cytosis.nicknames;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.Tuple;
import net.cytonic.cytosis.events.Events;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NicknameManager {
    //todo: Look into masking UUIDs in outgoing packets
    private final Map<UUID, NicknameData> nicknames = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> maskedUUIDs = new ConcurrentHashMap<>();

    public NicknameManager() {
        // remove them from this memory cache
        Events.onNetworkLeave(event -> {
            nicknames.remove(event.player());
            maskedUUIDs.remove(event.player());
        });
    }

    public static String translateSkin(CytosisPlayer player, String skin) {
        if (skin == null) return "<#BE9025>Steve/Alex skin</#BE9025>";
        if (skin.equals(player.getSkin().textures())) return "<#BE9025>My normal skin</#BE9025>";
        return "<#BE9025>Random Skin</#BE9025>";
    }

    public boolean isNicked(UUID player) {
        return nicknames.containsKey(player);
    }

    public void nicknamePlayer(UUID playerUuid, NicknameData data) {
        if (Cytosis.getPlayer(playerUuid).isEmpty()) return;
        if (nicknames.containsKey(playerUuid)) return;

        CytosisPlayer player = Cytosis.getPlayer(playerUuid).get();
        UUID masked = maskedUUIDs.computeIfAbsent(playerUuid, uuid -> UUID.randomUUID());

        nicknames.put(playerUuid, data);
        addToTrackedNicknames(playerUuid, data.nickname());
        sendNicknamePacketstoAll(player, masked, false);
        player.updatePreference(CytosisNamespaces.NICKNAME_DATA, data);
        player.updatePreference(CytosisNamespaces.NICKED_UUID, masked);

        Component msg = player.trueFormattedName()
                .append(Msg.aqua(" has been nicked to "))
                .append(player.formattedName()).
                append(Msg.aqua(" (Skin: %s)!", Msg.stripTags(translateSkin(player, data.value())
                        .replace("My", "Their"))));

        Cytosis.getSnooperManager().sendSnoop(CytosisSnoops.PLAYER_NICKNAME, Msg.snoop(msg));
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

    public void sendNicknamePacketsToPlayer(CytosisPlayer player, CytosisPlayer target, UUID masked, boolean freshViewer) {
        ArrayList<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();
        NicknameData data = getData(player.getUuid());
        if (data == null) return;
        if (data.signature() != null && data.value() != null) {
            properties.add(new PlayerInfoUpdatePacket.Property("textures", data.value(), data.signature()));
        }
        PlayerInfoUpdatePacket.Entry entry = new PlayerInfoUpdatePacket.Entry(masked, data.nickname(), properties, false,
                0, GameMode.SURVIVAL, null, null, 1);

        if (!freshViewer) {
            // remove the old player info and entity -- avoids nickname detection
            target.sendPackets(
                    new PlayerInfoRemovePacket(player.getUuid()), // look into this
                    new DestroyEntitiesPacket(player.getEntityId())
            );
        }

        target.sendPackets(
                new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry),
                new SpawnEntityPacket(player.getEntityId(), masked, EntityType.PLAYER.id(), player.getPosition(), player.getPosition().yaw(), 0, (short) 0, (short) 0, (short) 0),
                new EntityMetaDataPacket(player.getEntityId(), Map.of(17, Metadata.Byte((byte) 127)))
        );
        Cytosis.getRankManager().setupCosmetics(player, data.rank());
    }

    public void disableNickname(UUID playerUuid) {
        if (Cytosis.getPlayer(playerUuid).isEmpty()) return;
        if (!nicknames.containsKey(playerUuid)) return;

        CytosisPlayer player = Cytosis.getPlayer(playerUuid).get();

        NicknameData data = nicknames.remove(playerUuid);
        if (data == null) return;
        Cytosis.getDatabaseManager().getRedisDatabase().removeFromHash("cytosis:nicknames", playerUuid.toString());
        sendRemovePackets(player);
        Cytosis.getRankManager().setupCosmetics(player, player.getTrueRank());
        player.updatePreference(CytosisNamespaces.NICKNAME_DATA, null);
        player.updatePreference(CytosisNamespaces.NICKED_UUID, null);
    }

    public void sendRemovePackets(CytosisPlayer player) {
        final UUID masked = maskedUUIDs.get(player.getUuid());
        for (Player viewer : player.getViewers()) {
            ArrayList<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();

            if (player.getTrueSkin() == null) return;
            if (player.getTrueSkin().signature() != null && player.getTrueSkin().textures() != null) {
                properties.add(new PlayerInfoUpdatePacket.Property("textures", player.getTrueSkin().textures(), player.getTrueSkin().signature()));
            }
            PlayerInfoUpdatePacket.Entry entry = new PlayerInfoUpdatePacket.Entry(player.getUuid(), player.getTrueUsername(), properties, false,
                    0, GameMode.SURVIVAL, null, null, 1);
            // remove the old player info and entity
            viewer.sendPackets(
                    new PlayerInfoRemovePacket(masked),
                    new DestroyEntitiesPacket(player.getEntityId())
            );
            viewer.sendPackets(
                    new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry),
                    new SpawnEntityPacket(player.getEntityId(), player.getUuid(), EntityType.PLAYER.id(), player.getPosition(), player.getPosition().yaw(), 0, (short) 0, (short) 0, (short) 0),
                    new EntityMetaDataPacket(player.getEntityId(), Map.of(17, Metadata.Byte((byte) 127)))
            );
        }
    }

    public void loadNickedPlayer(CytosisPlayer player) {
        NicknameData data = Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisNamespaces.NICKNAME_DATA);
        UUID maskedUuid = player.getPreference(CytosisNamespaces.NICKED_UUID);
        if (maskedUuid == null) {
            maskedUuid = UUID.randomUUID();
        }
        if (data == null) return;
        this.maskedUUIDs.put(player.getUuid(), maskedUuid);
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
        return new HashSet<>(Cytosis.getDatabaseManager().getRedisDatabase().getHash("cytosis:nicknames").values());
    }

    private void addToTrackedNicknames(UUID playerUuid, String nickname) {
        Cytosis.getDatabaseManager().getRedisDatabase().addToHash("cytosis:nicknames", playerUuid.toString(), nickname);
        Cytosis.getDatabaseManager().getRedisDatabase().addToHash("cytosis:nicknames_reverse", nickname, playerUuid.toString());
    }

    public @Nullable UUID deanonymizePlayer(String nickname) {
        // on this server
        for (Map.Entry<UUID, NicknameData> entry : nicknames.entrySet()) {
            if (entry.getValue().nickname().equalsIgnoreCase(nickname)) {
                return entry.getKey();
            }
        }

        String raw = Cytosis.getDatabaseManager().getRedisDatabase().getFromHash("cytosis:nicknames_reverse", nickname);
        if (raw == null) return null;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Nullable
    public NicknameData getData(UUID playerUuid) {
        return nicknames.get(playerUuid);
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
