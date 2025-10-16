package net.cytonic.cytosis;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.NoArgsConstructor;

import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.containers.servers.PlayerChangeServerContainer;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.BanData;
import net.cytonic.cytosis.data.objects.BiMap;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.cytosis.utils.Utils;

/**
 * A class that holds data about the status of the Cytonic network
 */
@Getter
@NoArgsConstructor
@CytosisComponent(dependsOn = {RankManager.class})
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
    private MysqlDatabase db;

    @Override
    public void init() {
        this.cytosisContext = Cytosis.CONTEXT;
        this.db = cytosisContext.getComponent(MysqlDatabase.class);
        importData();
        cytosisContext.getComponent(CytonicNetwork.class)
            .getServers()
            .put(CytosisContext.SERVER_ID,
                new CytonicServer(Utils.getServerIP(),
                    CytosisContext.SERVER_ID,
                    CytosisSettings.SERVER_PORT,
                    cytosisContext.getServerGroup().type(),
                    cytosisContext.getServerGroup().group())
            );
    }

    /**
     * Imports data from Redis and Cydian
     */
    public void importData() {
        RedisDatabase redis = Cytosis.CONTEXT.getComponent(RedisDatabase.class);
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

        importPlayers(db);
        importBans(db);
        importMutes(db);
    }

    private void importPlayers(MysqlDatabase db) {
        PreparedStatement players = db.prepare("SELECT * FROM cytonic_players");
        db.query(players).whenComplete((rs, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading players!", throwable);
                return;
            }
            try {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    lifetimePlayers.put(uuid, rs.getString("name"));
                    lifetimeFlattened.put(uuid, rs.getString("name").toLowerCase());
                    cachedPlayerRanks.put(uuid, PlayerRank.valueOf(rs.getString("rank")));
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading players!", e);
            }
        });
    }

    private void importBans(MysqlDatabase db) {
        PreparedStatement bans = db.prepare("SELECT * FROM cytonic_bans");
        db.query(bans).whenComplete((rs, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading bans!", throwable);
                return;
            }
            try {
                while (rs.next()) {
                    Instant expiry = Instant.parse(rs.getString("to_expire"));
                    if (expiry.isBefore(Instant.now())) {
                        this.db.unbanPlayer(UUID.fromString(rs.getString("uuid")));
                    } else {
                        BanData banData = new BanData(rs.getString("reason"), expiry, true);
                        bannedPlayers.put(UUID.fromString(rs.getString("uuid")), banData);
                    }
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading bans!", e);
            }
        });
    }

    private void importMutes(MysqlDatabase db) {
        PreparedStatement mutes = db.prepare("SELECT * FROM cytonic_mutes");
        db.query(mutes).whenComplete((rs, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading mutes!", throwable);
                return;
            }
            try {
                while (rs.next()) {
                    Instant expiry = Instant.parse(rs.getString("to_expire"));
                    if (expiry.isBefore(Instant.now())) {
                        this.db.unmutePlayer(UUID.fromString(rs.getString("uuid")));
                    } else {
                        mutedPlayers.put(UUID.fromString(rs.getString("uuid")), true);
                    }
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading mutes!", e);
            }
        });
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

        db.getPlayerRank(uuid).thenAccept(playerRank -> cachedPlayerRanks.put(uuid, playerRank))
            .exceptionally((throwable) -> {
                Logger.error("An error occurred whilst loading ranks!", throwable);
                return null;
            });

        db.isBanned(uuid).thenAccept(banData -> bannedPlayers.put(uuid, banData)).exceptionally(throwable -> {
            Logger.error("An error occurred whilst loading bans!", throwable);
            return null;
        });

        db.isMuted(uuid).thenAccept(aBoolean -> mutedPlayers.put(uuid, aBoolean)).exceptionally(throwable -> {
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
    public void processPlayerServerChange(PlayerChangeServerContainer container) {
        networkPlayersOnServers.remove(container.player());
        networkPlayersOnServers.put(container.player(), container.newServer());
    }
}