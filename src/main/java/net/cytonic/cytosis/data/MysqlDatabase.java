package net.cytonic.cytosis.data;

import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.environments.EnvironmentManager;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.PosSerializer;

/**
 * A class handling Cytosis database transactions within its environment. Things like player ranks, punishments, and
 * some worlds are stored in {@link GlobalDatabase}
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
        String prefix = Cytosis.CONTEXT.getComponent(EnvironmentManager.class).getEnvironment().getPrefix();
        this.worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisDatabaseWorker")
            .uncaughtExceptionHandler(
                (t, e) -> Logger.error("An uncaught exception occurred on the thread: " + t.getName(), e)).factory());
        this.host = CytosisSettings.DATABASE_HOST;
        this.port = CytosisSettings.DATABASE_PORT;
        this.database = prefix + CytosisSettings.DATABASE_NAME;
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
                Logger.info("Successfully connected to the Environmental MySQL Database!");
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
        createWorldTable();
        createPlayerJoinsTable();
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