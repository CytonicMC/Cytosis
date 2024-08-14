package net.cytonic.cytosis;

import lombok.Getter;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.objects.PlayerServer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.enums.PlayerRank;
import net.cytonic.objects.BanData;
import net.cytonic.objects.BiMap;
import net.cytonic.objects.CytonicServer;
import net.cytonic.objects.PlayerPair;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.cytonic.cytosis.data.DatabaseTemplate.QUERY;

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
    private final Map<String, PlayerServer> networkPlayersOnServers = new ConcurrentHashMap<>();
    private final Map<UUID, BanData> bannedPlayers = new ConcurrentHashMap<>();

    /**
     * The default constructor
     */
    public CytonicNetwork() {
    }

    /**
     * Imports online player data from redis
     *
     * @param redis The redis instance
     */
    public void importData(RedisDatabase redis) {
        onlinePlayers.clear();
        onlineFlattened.clear();
        servers.clear();
        networkPlayersOnServers.clear();


        QUERY."SELECT * FROM cytonic_players".whenComplete((rs, throwable) -> {
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

        QUERY."SELECT * FROM cytonic_ranks".whenComplete((rs, throwable) -> {
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

        QUERY."SELECT * FROM cytonic_bans".whenComplete((rs, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading bans!", throwable);
                return;
            }
            try {
                while (rs.next()) {
                    Instant expiry = Instant.parse(rs.getString("to_expire"));
                    if (expiry.isBefore(Instant.now())) {
                        Cytosis.getDatabaseManager().getMysqlDatabase().unbanPlayer(UUID.fromString(rs.getString("uuid")));
                    }
                    BanData banData = new BanData(rs.getString("reason"), expiry, true);
                    bannedPlayers.put(UUID.fromString(rs.getString("uuid")), banData);
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading ranks!", e);
            }
        });
        networkPlayersOnServers.clear();


        redis.getSet(RedisDatabase.ONLINE_PLAYER_KEY).forEach(s -> {
            PlayerPair pp = PlayerPair.deserialize(s);
            onlinePlayers.put(pp.uuid(), pp.name());
            onlineFlattened.put(pp.uuid(), pp.name().toLowerCase());
        });
        redis.getSet(RedisDatabase.ONLINE_SERVER_KEY).forEach(s -> servers.put(CytonicServer.deserialize(s).id(), CytonicServer.deserialize(s)));
        redis.getSet(RedisDatabase.ONLINE_PLAYER_SERVER_KEY).forEach(s -> networkPlayersOnServers.put(s.split("\\|:\\|")[0], PlayerServer.deserialize(s)));
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

        // update it to see if the player's rank changed since the server started
        QUERY."SELECT rank_id FROM cytonic_ranks where uuid = '\{uuid.toString()}'".whenComplete((rs, throwable) -> {
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

        QUERY."SELECT * FROM cytonic_bans WHERE uuid = '\{uuid.toString()}'".whenComplete((rs, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading bans!", throwable);
                return;
            }
            try {
                while (rs.next()) {
                    bannedPlayers.put(uuid, new BanData(rs.getString("reason"), Instant.parse(rs.getString("to_expire")), true));
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading bans!", e);
            }
        });
        //todo: add the player to the networkPlayersOnServers?
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
        networkPlayersOnServers.remove(name);
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
}