package net.cytonic.cytosis;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.GlobalDatabase;
import net.cytonic.cytosis.data.GlobalDatabase.PlayerEntry;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.BanData;
import net.cytonic.cytosis.data.objects.BiMap;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.data.packets.servers.PlayerChangeServerPacket;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.cytosis.utils.Utils;


/**
 * A class that holds data about the status of the Cytonic network
 */
@Getter
@NoArgsConstructor
@CytosisComponent(dependsOn = {RankManager.class, RedisDatabase.class, GlobalDatabase.class})
public class CytonicNetwork implements Bootstrappable {

    private final BiMap<UUID, String> lifetimePlayers = new BiMap<>();
    private final BiMap<UUID, String> lifetimeFlattened = new BiMap<>(); // uuid, lowercased name
    //          ** This is for reference and should not be used to set data **
    private final Map<UUID, PlayerRank> cachedPlayerRanks = new ConcurrentHashMap<>(); // <player, rank>
    private final BiMap<UUID, String> onlinePlayers = new BiMap<>();
    private final BiMap<UUID, String> onlineFlattened = new BiMap<>(); // uuid, lowercased name
    private final Map<String, CytonicServer> servers = new ConcurrentHashMap<>(); // online servers
    private final Map<UUID, String> networkPlayersOnServers = new ConcurrentHashMap<>(); // uuid, server id
    private final Map<UUID, BanData> bannedPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> mutedPlayers = new ConcurrentHashMap<>();

    private CytosisContext cytosisContext;
    private GlobalDatabase gdb;

    @Override
    public void init() {
        this.cytosisContext = Cytosis.CONTEXT;
        this.gdb = cytosisContext.getComponent(GlobalDatabase.class);
        importData();
        getServers()
            .put(CytosisContext.SERVER_ID,
                new CytonicServer(Utils.getServerIP(),
                    CytosisContext.SERVER_ID,
                    cytosisContext.getComponent(CytosisSettings.class).getServerConfig().getPort(),
                    cytosisContext.getServerGroup().type(),
                    cytosisContext.getServerGroup().group())
            );
    }

    /**
     * Adds a player to the cache
     *
     * @param name The player's name
     * @param uuid The player's UUID
     */
    public void addPlayer(String name, UUID uuid) {
        onlinePlayers.put(uuid, name);
        onlineFlattened.put(uuid, name.toLowerCase());
        lifetimePlayers.put(uuid, name);
        lifetimeFlattened.put(uuid, name.toLowerCase());
        // the player has not played before
        cachedPlayerRanks.putIfAbsent(uuid, PlayerRank.DEFAULT);

        gdb.getPlayerRank(uuid).thenAccept(playerRank -> {
                cachedPlayerRanks.put(uuid, playerRank);
                cytosisContext.getComponent(RedisDatabase.class)
                    .addToGlobalHash("player_ranks", uuid.toString(), playerRank.name());
            })
            .exceptionally((throwable) -> {
                Logger.error("An error occurred whilst loading ranks!", throwable);
                return null;
            });

        gdb.isBanned(uuid).thenAccept(banData -> bannedPlayers.put(uuid, banData)).exceptionally(throwable -> {
            Logger.error("An error occurred whilst loading bans!", throwable);
            return null;
        });

        gdb.isMuted(uuid).thenAccept(aBoolean -> mutedPlayers.put(uuid, aBoolean)).exceptionally(throwable -> {
            Logger.error("An error occurred whilst loading mutes!", throwable);
            return null;
        });
    }

    /**
     * Updates the player's cached rank.
     *
     * @param uuid The player's UUID
     * @param rank The player's new rank
     */
    public void updateCachedPlayerRank(UUID uuid, PlayerRank rank) {
        cachedPlayerRanks.put(uuid, rank);
    }

    /**
     * Removes the player from the cache
     *
     * @param name The player's name
     * @param uuid The player's UUID
     */
    public void removePlayer(String name, UUID uuid) {
        onlinePlayers.remove(uuid, name);
        onlineFlattened.remove(uuid, name.toLowerCase());
        String playerServer = networkPlayersOnServers.get(uuid);
        networkPlayersOnServers.remove(uuid, playerServer);
    }

    /**
     * Determines if the specified UUID has played before
     *
     * @param uuid the uuid to try
     * @return if the player has played on the network before.
     */
    public boolean hasPlayedBefore(UUID uuid) {
        return lifetimePlayers.containsKey(uuid);
    }

    /**
     * Processes a player server change.
     *
     * @param container The container received over NATS or some message broker
     */
    public void processPlayerServerChange(PlayerChangeServerPacket container) {
        networkPlayersOnServers.remove(container.player());
        networkPlayersOnServers.put(container.player(), container.newServer());
    }

    /**
     * Imports data from Redis and Cydian
     */
    public void importData() {
        RedisDatabase redis = cytosisContext.getComponent(RedisDatabase.class);
        onlinePlayers.clear();
        onlineFlattened.clear();
        servers.clear();
        networkPlayersOnServers.clear();

        redis.getHash("online_players").forEach((rawuuid, name) -> {
            UUID uuid = UUID.fromString(rawuuid);
            onlinePlayers.put(uuid, name);
            onlineFlattened.put(uuid, name.toLowerCase());
        });

        redis.getHash("player_servers")
            .forEach((id, server) -> networkPlayersOnServers.put(UUID.fromString(id), server));

        importPlayers();
    }

    private void importPlayers() {
        for (PlayerEntry p : gdb.loadPlayers()) {
            lifetimePlayers.put(p.uuid(), p.username());
            lifetimeFlattened.put(p.uuid(), p.username().toLowerCase());
            cachedPlayerRanks.put(p.uuid(), p.rank());

            if (p.banData() != null) {
                if (p.banData().expiry() != null && p.banData().expiry().isBefore(Instant.now())) {
                    this.gdb.unbanPlayer(p.uuid());
                    RedisDatabase redis = cytosisContext.getComponent(RedisDatabase.class);
                    redis.removeFromGlobalHash("banned_players", p.uuid().toString());
                } else {
                    bannedPlayers.put(p.uuid(), p.banData());
                }
            }

            if (p.muteExpiry() != null && p.muteExpiry().isBefore(Instant.now())) {
                this.gdb.unmutePlayer(p.uuid());
            } else {
                mutedPlayers.put(p.uuid(), true);
            }
        }
    }

    public String getMiniName(UUID player) {
        String name = lifetimePlayers.getByKey(player);
        String rank = MiniMessage.miniMessage().serialize(cachedPlayerRanks.get(player).getPrefix());
        return rank + name;
    }

    /**
     * Gets if the specific player is connected to any server in the network
     *
     * @param player the player to check
     * @return if the player is on a server connected to the network.
     */
    public boolean isOnline(UUID player) {
        return onlinePlayers.containsKey(player);
    }
}
