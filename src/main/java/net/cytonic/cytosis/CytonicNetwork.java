package net.cytonic.cytosis;

import lombok.Getter;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.containers.servers.PlayerChangeServerContainer;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.BanData;
import net.cytonic.cytosis.data.objects.BiMap;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.logging.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that holds data about the status of the Cytonic network
 */
@Getter
public class CytonicNetwork {
    private final BiMap<UUID, String> lifetimePlayers = new BiMap<>();
    private final BiMap<UUID, String> lifetimeFlattened = new BiMap<>(); // uuid, lowercased name
    private final Map<UUID, PlayerRank> playerRanks = new ConcurrentHashMap<>(); // <player, rank> ** This is for reference and should not be used to set data **
    private final BiMap<UUID, String> onlinePlayers = new BiMap<>();
    private final BiMap<UUID, String> onlineFlattened = new BiMap<>(); // uuid, lowercased name
    private final Map<String, CytonicServer> servers = new ConcurrentHashMap<>(); // online servers
    private final Map<UUID, String> networkPlayersOnServers = new ConcurrentHashMap<>(); // uuid, server id
    private final Map<UUID, BanData> bannedPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> mutedPlayers = new ConcurrentHashMap<>();

    /**
     * The default constructor
     */
    public CytonicNetwork() {
    }

    /**
     * Imports data from Redis and Cydian
     */
    public void importData() {
        Cytosis.getNatsManager().fetchServers();
        RedisDatabase redis = Cytosis.getDatabaseManager().getRedisDatabase();
        MysqlDatabase db = Cytosis.getDatabaseManager().getMysqlDatabase();
        onlinePlayers.clear();
        onlineFlattened.clear();
        servers.clear();
        networkPlayersOnServers.clear();


        PreparedStatement players = db.prepare("SELECT * FROM cytonic_players");
        db.query(players).whenComplete((rs, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading players!", throwable);
                return;
            }
            try {
                while (rs.next()) {
                    lifetimePlayers.put(UUID.fromString(rs.getString("uuid")), rs.getString("name"));
                    lifetimeFlattened.put(UUID.fromString(rs.getString("uuid")), rs.getString("name").toLowerCase());
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading players!", e);
            }
        });

        PreparedStatement ranks = db.prepare("SELECT * FROM cytonic_ranks");
        db.query(ranks).whenComplete((rs, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading ranks!", throwable);
                return;
            }
            try {
                while (rs.next()) {
                    playerRanks.put(UUID.fromString(rs.getString("uuid")), PlayerRank.valueOf(rs.getString("rank_id")));
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading ranks!", e);
            }
        });

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
                        Cytosis.getDatabaseManager().getMysqlDatabase().unbanPlayer(UUID.fromString(rs.getString("uuid")));
                    } else {
                        BanData banData = new BanData(rs.getString("reason"), expiry, true);
                        bannedPlayers.put(UUID.fromString(rs.getString("uuid")), banData);
                    }
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading bans!", e);
            }
        });

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
                        Cytosis.getDatabaseManager().getMysqlDatabase().unmutePlayer(UUID.fromString(rs.getString("uuid")));
                    } else mutedPlayers.put(UUID.fromString(rs.getString("uuid")), true);
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
        playerRanks.putIfAbsent(uuid, PlayerRank.DEFAULT);

        MysqlDatabase db = Cytosis.getDatabaseManager().getMysqlDatabase();

        PreparedStatement rank = db.prepare("SELECT * FROM cytonic_ranks WHERE uuid = ?");
        try {
            rank.setString(1, uuid.toString());
        } catch (SQLException e) {
            // this is a problem, and an error should 100% be thrown here and interrrupt something
            throw new RuntimeException(e);
        }


        // update it to see if the player's rank changed since the server started
        db.query(rank).whenComplete((rs, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading ranks!", throwable);
                return;
            }
            try {
                while (rs.next()) {
                    playerRanks.put(uuid, PlayerRank.valueOf(rs.getString("rank_id")));
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading ranks!", e);
            }
        });

        PreparedStatement bans = db.prepare("SELECT * FROM cytonic_bans WHERE uuid = ?");
        try {
            bans.setString(1, uuid.toString());
        } catch (SQLException e) {
            // this is a problem, and an error should 100% be thrown here and interrrupt something
            throw new RuntimeException(e);
        }
        db.query(bans).whenComplete((rs, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading bans!", throwable);
                return;
            }
            try {
                while (rs.next()) {
                    bannedPlayers.put(uuid, new BanData(rs.getString("reason"), Instant.parse(rs.getString("to_expire")), true));
                }
            } catch (SQLException e) {
                throw new RuntimeException("An error occurred whilst loading bans!", e);
            }
        });

        PreparedStatement muted = db.prepare("SELECT * FROM cytonic_mutes WHERE uuid = ?");
        try {
            muted.setString(1, uuid.toString());
        } catch (SQLException e) {
            // this is a problem, and an error should 100% be thrown here and interrrupt something
            throw new RuntimeException(e);
        }
        db.query(muted).whenComplete((rs, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading mutes!", throwable);
                return;
            }
            try {
                while (rs.next()) {
                    mutedPlayers.put(uuid, true);
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading mutes!", e);
            }
        });
    }

    /**
     * Updates the player's cached rank.
     *
     * @param uuid The player's UUID
     * @param rank The player's new rank
     */
    public void updatePlayerRank(UUID uuid, PlayerRank rank) {
        playerRanks.put(uuid, rank);
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
     * @param container The container recived over NATS or some message broker
     */
    public void processPlayerServerChange(PlayerChangeServerContainer container) {
        networkPlayersOnServers.remove(container.player());
        networkPlayersOnServers.put(container.player(), container.newServer());
    }
}