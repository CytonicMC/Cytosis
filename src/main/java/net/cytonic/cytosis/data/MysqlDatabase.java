package net.cytonic.cytosis.data;

import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.SneakyThrows;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.BanData;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.environments.EnvironmentManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.PosSerializer;

/**
 * A class handling Cytosis database transactions
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
@CytosisComponent(dependsOn = {FileManager.class})
public class MysqlDatabase implements Bootstrappable {

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
    public MysqlDatabase() {
        this.worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisDatabaseWorker")
            .uncaughtExceptionHandler(
                (t, e) -> Logger.error("An uncaught exception occurred on the thread: " + t.getName(), e)).factory());
        this.host = CytosisSettings.DATABASE_HOST;
        this.port = CytosisSettings.DATABASE_PORT;
        this.database = Cytosis.CONTEXT.getComponent(EnvironmentManager.class).getEnvironment().getPrefix()
            + CytosisSettings.DATABASE_NAME;
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
                Logger.info("Successfully connected to the MySQL Database!");
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
                Logger.info("Database connection closed!");
            } catch (SQLException e) {
                Logger.error("""
                    An error occurred whilst disconnecting from the database.\
                    Please report the following stacktrace to CytonicMC:\
                    """, e);
            }
        }
    }

    /**
     * Creates the database tables
     */
    public void createTables() {
        createChatTable();
        createBansTable();
        createPlayersTable();
        createWorldTable();
        createPlayerJoinsTable();
        createMutesTable();
        createPlayerMessagesTable();
    }

    /**
     * Creates the chat messages table
     */
    private void createChatTable() {
        try {
            getConnection().prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_chat (
                    id INT NOT NULL AUTO_INCREMENT,
                    timestamp TIMESTAMP,
                    uuid VARCHAR(36),
                    message TEXT,
                    PRIMARY KEY(id)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the `cytonic_chat` table.", e);
        }
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

    /**
     * Create the player join logging table
     */
    private void createPlayerJoinsTable() {
        try (PreparedStatement ps = getConnection().prepareStatement(
            "CREATE TABLE IF NOT EXISTS cytonic_player_joins (joined TIMESTAMP, uuid VARCHAR(36), ip TEXT)")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the `cytonic_player_joins` table.", e);
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
     * Creates the player messages table
     */
    private void createPlayerMessagesTable() {
        try {
            getConnection().prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_player_messages (
                    id INT NOT NULL AUTO_INCREMENT,
                    timestamp TIMESTAMP,
                    sender VARCHAR(36),
                    target VARCHAR(36),
                    message TEXT,
                    PRIMARY KEY(id)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the `cytonic_player_messages` table.", e);
        }
    }

    /**
     * Adds a player message to the database
     *
     * @param sender  the sender of the message
     * @param target  the target of the message
     * @param message the message
     * @return a future that completes when the message has been added
     */
    public CompletableFuture<Void> addPlayerMessage(UUID sender, UUID target, String message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("The database must be connected to add player messages.");
            }
            try {
                PreparedStatement ps = getConnection().prepareStatement("""
                    INSERT INTO cytonic_player_messages (timestamp, sender, target, message)
                    VALUES (CURRENT_TIMESTAMP, ?, ?, ?)
                    """);
                ps.setString(1, sender.toString());
                ps.setString(2, target.toString());
                ps.setString(3, message);
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst adding a player message from " + sender + " to " + target + ".",
                    e);
                future.completeExceptionally(e);
            }
        });
        return future;
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
     * Add a chat message to the log
     *
     * @param uuid    The UUID of the sender
     * @param message The message to log
     */
    public void addChat(UUID uuid, String message) {
        worker.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO cytonic_chat (timestamp, uuid, message) VALUES (CURRENT_TIMESTAMP,?,?)");
                ps.setString(1, uuid.toString());
                ps.setString(2, message);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
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

    /**
     * Logs a player's join event to the database.
     *
     * @param uuid The unique identifier of the player.
     * @param ip   The IP address of the player.
     */
    public void logPlayerJoin(UUID uuid, SocketAddress ip) {
        worker.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO cytonic_player_joins (joined, uuid, ip) VALUES (CURRENT_TIMESTAMP,?,?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, ip.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                Logger.error("Failed to add a player to the database!", e);
            }
        });
    }

    /**
     * Queries the database with the specified SQL
     *
     * @param sql The SQL query
     * @return The {@link ResultSet} of the query
     */
    public CompletableFuture<ResultSet> query(String sql) {
        CompletableFuture<ResultSet> future = new CompletableFuture<>();
        worker.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ResultSet rs = ps.executeQuery();
                future.complete(rs);
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Queries the database with the specified prepared statement
     *
     * @param preparedStatement the query
     * @return the result set of the query, completed once the query is complete
     */
    public CompletableFuture<ResultSet> query(PreparedStatement preparedStatement) {
        CompletableFuture<ResultSet> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                future.complete(preparedStatement.executeQuery());
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Queries the database with the specified SQL, SYNCHRONOUSLY
     *
     * @param sql The SQL query
     * @return The {@link ResultSet} of the query
     */
    public ResultSet querySync(String sql) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            return ps.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet querySync(PreparedStatement ps) {
        try {
            return ps.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prepares a statement
     *
     * @param sql the sql to use
     * @return the prepared statement object
     */
    @SneakyThrows
    public PreparedStatement prepare(String sql) {
        return connection.prepareStatement(sql);
    }

    /**
     * Updates the database with the specified SQL
     *
     * @param sql The SQL update
     * @return A {@link CompletableFuture} for when the update is completed
     */
    public CompletableFuture<Void> update(PreparedStatement sql) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                sql.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst updating the database!", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Updates the database with the specified SQL
     *
     * @param sql The SQL update
     * @return A {@link CompletableFuture} for when the update is completed
     */
    CompletableFuture<Void> update(String sql) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst updating the database!", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}