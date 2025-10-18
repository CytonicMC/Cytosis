package net.cytonic.cytosis.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.BanData;
import net.cytonic.cytosis.data.objects.preferences.PreferenceData;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.PosSerializer;
import net.cytonic.cytosis.utils.Utils;

/**
 * The database object that handles data that is stored across network environments
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class GlobalDatabase implements Bootstrappable {

    private final ExecutorService worker;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    @Getter
    private Connection connection;

    /**
     * Creates and initializes a new MysqlDatabase
     */
    public GlobalDatabase() {
        this.worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisDatabaseWorker")
            .uncaughtExceptionHandler(
                (t, e) -> Logger.error("An uncaught exception occurred on the thread: " + t.getName(), e)).factory());
        this.host = CytosisSettings.DATABASE_HOST;
        this.port = CytosisSettings.DATABASE_PORT;
        this.database = CytosisSettings.GLOBAL_DATABASE;
        this.username = CytosisSettings.DATABASE_USER;
        this.password = CytosisSettings.DATABASE_PASSWORD;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.error("Failed to load database driver", e);
        }
    }

    @Override
    public void init() {
        connect();
        createTables();
    }

    @Override
    public void shutdown() {
        disconnect();
    }

    /**
     * Connects to the database, blocking until completed
     */
    public void connect() {
        if (!isConnected()) {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?autoReconnect=true&allowPublicKeyRetrieval=true", username, password);
                Logger.info("Successfully connected to the Global MySQL Database!");
            } catch (SQLException e) {
                Logger.error("Invalid Database Credentials!", e);
                MinecraftServer.stopCleanly();
            }
        }
    }

    /**
     * Checks if the database is connected
     *
     * @return if the database is connected
     */
    public boolean isConnected() {
        return (connection != null);
    }

    /**
     * Disconnects from the database server
     */
    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
                Logger.info("Global Database connection closed!");
            } catch (SQLException e) {
                Logger.error("An error occurred whilst disconnecting from the global database.", e);
            }
        }
    }

    /**
     * Creates the database tables
     */
    public void createTables() {
        createBansTable();
        createPlayersTable();
        createWorldTable();
        createMutesTable();
        createPreferencesTable();
        createFriendTable();
    }

    /**
     * Creates the bans table
     */
    private void createBansTable() {
        try {
            getConnection().prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_bans (
                    uuid VARCHAR(36),
                    to_expire VARCHAR(100),
                    reason TINYTEXT,
                    PRIMARY KEY(uuid)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the `cytonic_bans` table.", e);
        }
    }

    /**
     * Creates the player data table
     */
    private void createPlayersTable() {
        try {
            getConnection().prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_players (
                    uuid VARCHAR(36),
                    `rank` VARCHAR(16),
                    name VARCHAR(16),
                    PRIMARY KEY(uuid)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the `cytonic_players` table.", e);
        }
    }

    /**
     * Creates the world table
     */
    public void createWorldTable() {
        try {
            getConnection().prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_preferences (
                    uuid VARCHAR(36) PRIMARY KEY,
                    preferences TEXT
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the `cytonic_preferences` table.", e);
        }
    }

    public void createPreferencesTable() {
        try {
            getConnection().prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_worlds (
                    world_name TEXT,
                    world_type TEXT,
                    last_modified TIMESTAMP,
                    world_data MEDIUMBLOB,
                    spawn_point TEXT,
                    extra_data TEXT,
                    UUID VARCHAR(36),
                    PRIMARY KEY(world_name)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the `cytonic_worlds` table.", e);
        }
    }

    public void createFriendTable() {
        try {
            getConnection().prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_friends (
                    uuid VARCHAR(36),
                    friends TEXT,
                    PRIMARY KEY (uuid)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the `cytonic_friends` table.", e);
        }
    }

    /**
     * Creates the bans table
     */
    private void createMutesTable() {
        try {
            getConnection().prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_mutes (
                    uuid VARCHAR(36),
                    to_expire VARCHAR(100),
                    PRIMARY KEY(uuid)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the `cytonic_mutes` table.", e);
        }
    }

    /**
     * Mutes a player
     *
     * @param uuid     the player to mute
     * @param toExpire When the mute expires
     * @return a future that completes when the player is muted
     */
    public CompletableFuture<Void> mutePlayer(UUID uuid, Instant toExpire) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("The database must be connected to mute players.");
            }
            try {
                Cytosis.CONTEXT.getComponent(CytonicNetwork.class).getMutedPlayers().put(uuid, true);
                PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT IGNORE INTO cytonic_mutes (uuid, to_expire) VALUES (?,?)");
                ps.setString(1, uuid.toString());
                ps.setString(2, toExpire.toString());
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst muting the player " + uuid + ".", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * The concurrent friendly way to fetch a player's mute status
     *
     * @param uuid THe player to check
     * @return The CompletableFuture that holds the player's mute status
     */
    public CompletableFuture<Boolean> isMuted(UUID uuid) {
        if (!isConnected()) {
            throw new IllegalStateException("The database must be connected.");
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM cytonic_mutes WHERE uuid = ?");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Instant expiry = Instant.parse(rs.getString("to_expire"));
                    if (expiry.isBefore(Instant.now())) {
                        future.complete(false);
                        unmutePlayer(uuid);
                    } else {
                        future.complete(true);
                    }
                } else {
                    future.complete(false);
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst determining if the player " + uuid + " is muted.", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Unmutes a player
     *
     * @param uuid the player to unmute
     * @return a future that completes when the player is unmuted
     */
    public CompletableFuture<Void> unmutePlayer(UUID uuid) {
        if (!isConnected()) {
            throw new IllegalStateException("The database must be connected.");
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                Cytosis.CONTEXT.getComponent(CytonicNetwork.class).getMutedPlayers().remove(uuid);
                PreparedStatement ps = getConnection().prepareStatement("DELETE FROM cytonic_mutes WHERE uuid = ?");
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst unmuting the player " + uuid + ".", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Gets the player's rank. This returns {@link PlayerRank#DEFAULT} even if the player doesn't exist.
     *
     * @param uuid the player to fetch the id from
     * @return The player's {@link PlayerRank}
     * @throws IllegalStateException if the database isn't connected
     */
    @NotNull
    public CompletableFuture<PlayerRank> getPlayerRank(@NotNull final UUID uuid) {
        CompletableFuture<PlayerRank> future = new CompletableFuture<>();
        if (!isConnected()) {
            throw new IllegalStateException("The database must have an open connection to fetch a player's rank!");
        }
        worker.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement("SELECT `rank` FROM cytonic_players WHERE uuid = ?");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    future.complete(PlayerRank.valueOf(rs.getString("rank")));
                } else {
                    future.complete(PlayerRank.DEFAULT);
                    setPlayerRank(uuid, PlayerRank.DEFAULT);
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst fetching the rank of '" + uuid + "'");
            }
        });
        return future;
    }

    /**
     * Sets the given player's rank to the specified rank.
     *
     * @param uuid The player's UUID
     * @param rank The player's rank constant
     * @return a future that completes when the update is complete
     * @throws IllegalStateException if the database isn't connected
     */
    public CompletableFuture<Void> setPlayerRank(UUID uuid, PlayerRank rank) {
        if (!isConnected()) {
            throw new IllegalStateException("The database must have an open connection to set a player's rank!");
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO cytonic_players (uuid, `rank`)
                    VALUES (?,?)
                    ON DUPLICATE KEY UPDATE `rank` = VALUES(rank)
                    """);
                ps.setString(1, uuid.toString());
                ps.setString(2, rank.name());
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst setting the rank of '" + uuid + "'");
            }
        });
        return future;
    }

    /**
     * Bans a player
     *
     * @param uuid     the player to ban
     * @param reason   The reason to ban the player
     * @param toExpire When the ban expires
     * @return a future that completes when the player is banned
     */
    public CompletableFuture<Void> banPlayer(UUID uuid, String reason, Instant toExpire) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        worker.submit(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("The database must be connected to ban players.");
            }
            try {
                PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT IGNORE INTO cytonic_bans (uuid, to_expire, reason) VALUES (?,?,?)");
                ps.setString(1, uuid.toString());
                ps.setString(2, toExpire.toString());
                ps.setString(3, reason);
                ps.executeUpdate();

                RedisDatabase redis = Cytosis.CONTEXT.getComponent(RedisDatabase.class);
                BanData data = new BanData(reason, toExpire, true);
                redis.addToGlobalHash("banned_players", uuid.toString(), Cytosis.GSON.toJson(data));

                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst banning the player " + uuid + ".", e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * The concurrent friendly way to fetch a player's ban status
     *
     * @param uuid THe player to check
     * @return The CompletableFuture that holds the player's ban status
     */
    public CompletableFuture<BanData> isBanned(UUID uuid) {
        if (!isConnected()) {
            throw new IllegalStateException("The database must be connected.");
        }
        CompletableFuture<BanData> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM cytonic_bans WHERE uuid = ?");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Instant expiry = Instant.parse(rs.getString("to_expire"));
                    if (expiry.isBefore(Instant.now())) {
                        future.complete(new BanData(null, null, false));
                        unbanPlayer(uuid);
                    } else {
                        try {
                            BanData banData = new BanData(rs.getString("reason"), expiry, true);
                            future.complete(banData);
                        } catch (Exception e) {
                            Logger.error("An error occurred whilst determining if the player " + uuid + " is banned.",
                                e);
                            future.complete(new BanData(null, null, true));
                        }
                    }
                } else {
                    future.complete(new BanData(null, null, false));
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst determining if the player " + uuid + " is banned.", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Unbans a player
     *
     * @param uuid the player to unban
     * @return a future that completes when the player is unbanned
     */
    public CompletableFuture<Void> unbanPlayer(UUID uuid) {
        if (!isConnected()) {
            throw new IllegalStateException("The database must be connected.");
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("DELETE FROM cytonic_bans WHERE uuid = ?");
                ps.setString(1, uuid.toString());
                ps.executeUpdate();

                RedisDatabase redis = Cytosis.CONTEXT.getComponent(RedisDatabase.class);
                redis.removeFromGlobalHash("banned_players", uuid.toString());

                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst unbanning the player " + uuid + ".", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Finds a player's UUID by name
     *
     * @param name the player's name
     * @return a future that completes with the player's UUID
     */
    public CompletableFuture<UUID> findUuidByName(String name) {
        if (!isConnected()) {
            throw new IllegalStateException("The database must be connected.");
        }
        CompletableFuture<UUID> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM cytonic_players WHERE name = ?");
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    future.complete(UUID.fromString(rs.getString("uuid")));
                } else {
                    future.completeExceptionally(
                        new IllegalArgumentException("The player '" + name + "' doesn't exist!"));
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst determining " + name + "'s UUID.", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Adds a or updates a player's name in the data
     *
     * @param player The player to update
     * @return a future that completes when the update is complete
     */
    public CompletableFuture<Void> addPlayer(CytosisPlayer player) {
        if (!isConnected()) {
            throw new IllegalStateException("The database must be connected.");
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("""
                    INSERT IGNORE INTO cytonic_players (name, uuid, `rank`)
                    VALUES (?,?,?) ON DUPLICATE KEY UPDATE name = ?
                    """);
                ps.setString(1, player.getTrueUsername());
                ps.setString(2, player.getUuid().toString());
                ps.setString(3, player.getRank().name());
                ps.setString(4, player.getTrueUsername());
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst setting the name of " + player.getUuid() + ".", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public CompletableFuture<Void> addWorld(String worldName, String worldType, PolarWorld world, Pos spawnPoint,
        UUID worldUuid) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (!isConnected()) {
            throw new IllegalStateException("The database must have an open connection to add a world!");
        }

        world.setCompression(PolarWorld.CompressionType.ZSTD);

        worker.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO cytonic_worlds (world_name, world_type, last_modified, world_data, spawn_point, uuid)
                    VALUES (?,?, CURRENT_TIMESTAMP,?,?,?)
                    """);
                ps.setString(1, worldName);
                ps.setString(2, worldType);
                ps.setBytes(3, PolarWriter.write(world));
                ps.setString(4, PosSerializer.serialize(spawnPoint));
                ps.setString(5, worldUuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                Logger.error("An error occurred whilst adding a world!", e);
            }
            future.complete(null);
        });
        return future;
    }

    /**
     * Retrieves a world from the database.
     *
     * @param worldName The name of the world to fetch.
     * @return A {@link CompletableFuture} that completes with the fetched {@link PolarWorld}. If the world does not
     * exist in the database, the future will complete exceptionally with a {@link RuntimeException}.
     * @throws IllegalStateException If the database connection is not open.
     */
    public CompletableFuture<PolarWorld> getWorld(String worldName) {
        CompletableFuture<PolarWorld> future = new CompletableFuture<>();
        if (!isConnected()) {
            throw new IllegalStateException("The database must have an open connection to fetch a world!");
        }
        worker.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM cytonic_worlds WHERE world_name = ?")) {
                ps.setString(1, worldName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    PolarWorld world = PolarReader.read(rs.getBytes("world_data"));
                    CytosisSettings.SERVER_SPAWN_POS = PosSerializer.deserialize(rs.getString("spawn_point"));
                    future.complete(world);
                } else {
                    Logger.error("The result set is empty!");
                    throw new RuntimeException("World not found: " + worldName);
                }
            } catch (Exception e) {
                Logger.error("An error occurred whilst fetching a world!", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Retrieves a world from the database.
     *
     * @param worldName The name of the world to fetch.
     * @param worldType The world type of the world to fetch.
     * @return A {@link CompletableFuture} that completes with the fetched {@link PolarWorld}. If the world does not
     * exist in the database, the future will complete exceptionally with a {@link RuntimeException}.
     * @throws IllegalStateException If the database connection is not open.
     */
    public CompletableFuture<PolarWorld> getWorld(String worldName, String worldType) {
        CompletableFuture<PolarWorld> future = new CompletableFuture<>();
        if (!isConnected()) {
            throw new IllegalStateException("The database must have an open connection to fetch a world!");
        }
        worker.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM cytonic_worlds WHERE world_name = ? AND world_type = ?")) {
                ps.setString(1, worldName);
                ps.setString(2, worldType);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    PolarWorld world = PolarReader.read(rs.getBytes("world_data"));
                    CytosisSettings.SERVER_SPAWN_POS = PosSerializer.deserialize(rs.getString("spawn_point"));
                    future.complete(world);
                } else {
                    Logger.error("The result set is empty!");
                    throw new RuntimeException("World not found: " + worldName);
                }
            } catch (Exception e) {
                Logger.error("An error occurred whilst fetching a world!", e);
                future.completeExceptionally(e);
                throw new RuntimeException(e);
            }
        });
        return future;
    }

    public CompletableFuture<String> getWorldExtraData(String worldName, String worldType) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (!isConnected()) {
            throw new IllegalStateException(
                "The database must have an open connection to fetch the extra data from a world!");
        }
        worker.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                "SELECT extra_data FROM cytonic_worlds WHERE world_name = ? AND world_type = ?")) {
                ps.setString(1, worldName);
                ps.setString(2, worldType);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    future.complete(rs.getString("extra_data"));
                } else {
                    Logger.error("The result set is empty!");
                    throw new RuntimeException("World data not found: " + worldName);
                }
            } catch (Exception e) {
                Logger.error("An error occurred whilst fetching the extra data from a world!", e);
                future.completeExceptionally(e);
                throw new RuntimeException(e);
            }
        });
        return future;
    }

    public CompletableFuture<Boolean> worldExists(String worldName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if (!isConnected()) {
            throw new IllegalStateException("The database must have an open connection to fetch a world!");
        }
        worker.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                "SELECT world_name FROM cytonic_worlds WHERE world_name = ?")) {
                ps.setString(1, worldName);
                ResultSet rs = ps.executeQuery();
                future.complete(rs.next());
            } catch (Exception e) {
                Logger.error("An error occurred whilst fetching a world!", e);
                future.completeExceptionally(e);
                throw new RuntimeException(e);
            }
        });
        return future;
    }

    public List<PunishmentEntry> loadBans() {
        List<PunishmentEntry> list = new ArrayList<>();
        try {
            ResultSet rs = connection.prepareStatement("SELECT * FROM cytonic_bans").executeQuery();
            while (rs.next()) {
                list.add(new PunishmentEntry(UUID.fromString(rs.getString("uuid")),
                    Instant.parse(rs.getString("to_expire")), rs.getString("reason")));
            }
        } catch (SQLException e) {
            Logger.error("An error occurred whilst loading bans!", e);
        }
        return list;
    }

    public List<PunishmentEntry> loadMutes() {
        List<PunishmentEntry> list = new ArrayList<>();
        try {
            ResultSet rs = connection.prepareStatement("SELECT * FROM cytonic_mutes").executeQuery();
            while (rs.next()) {
                list.add(new PunishmentEntry(UUID.fromString(rs.getString("uuid")),
                    Instant.parse(rs.getString("to_expire")), ""));
            }
        } catch (SQLException e) {
            Logger.error("An error occurred whilst loading mutes!", e);
        }
        return list;
    }

    public List<PlayerEntry> loadPlayers() {
        List<PlayerEntry> list = new ArrayList<>();
        try {
            ResultSet rs = connection.prepareStatement("SELECT * FROM cytonic_players").executeQuery();
            while (rs.next()) {
                list.add(new PlayerEntry(UUID.fromString(rs.getString("uuid")),
                    rs.getString("name"), PlayerRank.valueOf(rs.getString("rank"))));
            }
        } catch (SQLException e) {
            Logger.error("An error occurred whilst loading players!", e);
        }
        return list;
    }

    public CompletableFuture<PreferenceData> loadPlayerPreferences(UUID player) {
        CompletableFuture<PreferenceData> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement load = connection.prepareStatement(
                    "SELECT * FROM cytonic_preferences WHERE uuid = ?");
                load.setString(1, player.toString());
                ResultSet rs = load.executeQuery();
                if (rs.next()) {
                    PreferenceData data = PreferenceData.deserialize(rs.getString("preferences"));
                    future.complete(data);
                } else {
                    future.completeExceptionally(new IllegalArgumentException("ResultSet is empty!"));
                }
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    public CompletableFuture<List<UUID>> loadFriends(UUID player) {
        CompletableFuture<List<UUID>> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM cytonic_friends WHERE uuid = ?");
                ps.setString(1, player.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    future.complete(Cytosis.GSON.fromJson(rs.getString("friends"), Utils.UUID_LIST));
                } else {
                    future.completeExceptionally(new IllegalArgumentException("ResultSet is empty!"));
                }
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    public void updateFriends(UUID player, List<UUID> friends) {
        worker.submit(() -> {

            try {
                PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO cytonic_friends (uuid, friends)
                    VALUES (?, ?) ON DUPLICATE KEY UPDATE friends = ?
                    """);
                friends.remove(player); // prevent self as a friend somehow
                String serialized = Cytosis.GSON.toJson(friends);
                ps.setString(1, player.toString());
                ps.setString(2, serialized);
                ps.setString(3, serialized);
                ps.executeUpdate();
            } catch (Exception e) {
                Logger.error("Failed to persist friend data!", e);
            }
        });
    }

    public void addNewPlayerPreferences(UUID player, PreferenceData data) {
        worker.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO cytonic_preferences VALUES(?,?)");
                ps.setString(1, player.toString());
                ps.setString(2, data.serialize());
                ps.executeUpdate();
            } catch (Exception e) {
                Logger.error("An error occurred whilst adding a new player's preferences!", e);
            }
        });
    }

    public void persistPlayerPreferences(UUID player, PreferenceData data) {
        worker.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement(
                    "UPDATE cytonic_preferences SET preferences = ? WHERE uuid = ?");
                ps.setString(1, player.toString());
                ps.setString(2, data.serialize());
                ps.executeUpdate();
            } catch (Exception e) {
                Logger.error("An error occurred whilst adding a new player's preferences!", e);
            }
        });
    }


    public record PunishmentEntry(UUID player, Instant expiry, String reason) {

    }

    public record PlayerEntry(UUID uuid, String username, PlayerRank rank) {

    }
}
