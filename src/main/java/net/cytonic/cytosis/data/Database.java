package net.cytonic.cytosis.data;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.ranks.PlayerRank;
import net.cytonic.cytosis.utils.PosSerializer;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import java.net.SocketAddress;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Database {

    private final ExecutorService worker;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean ssl;
    private Connection connection;

    public Database() {
        this.worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisDatabaseWorker").uncaughtExceptionHandler((t, e) -> Logger.error(STR."An uncaught exception occoured on the thread: \{t.getName()}", e)).factory());
        this.host = CytosisSettings.DATABASE_HOST;
        this.port = CytosisSettings.DATABASE_PORT;
        this.database = CytosisSettings.DATABASE_NAME;
        this.username = CytosisSettings.DATABASE_USER;
        this.password = CytosisSettings.DATABASE_PASSWORD;
        this.ssl = CytosisSettings.DATABASE_USE_SSL;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.error("Failed to load database driver", e);
        }
    }

    public boolean isConnected() {
        return (connection != null);
    }

    public void connect() {
        worker.submit(() -> {
            if (!isConnected()) {
                try {
                    connection = DriverManager.getConnection(STR."jdbc:mysql://\{host}:\{port}/\{database}?useSSL=\{ssl}&autoReconnect=true&allowPublicKeyRetrieval=true", username, password);
                    Logger.info("Successfully connected to the MySQL Database!");
                    Cytosis.loadWorld();
                } catch (SQLException e) {
                    Logger.error("Invalid Database Credentials!", e);
                    MinecraftServer.stopCleanly();
                }
            }
        });

    }

    public void disconnect() {
        worker.submit(() -> {
            if (isConnected()) {
                try {
                    connection.close();
                    Logger.info("Database connection closed!");
                } catch (SQLException e) {
                    Logger.error("An error occurred whilst disconnecting from the database. Please report the following stacktrace to Foxikle: ", e);
                }
            }
        });
    }

    public void createTables() {
        createRanksTable();
        createChatTable();
        createWorldTable();
        createPlayersTable();
    }

    private Connection getConnection() {
        return connection;
    }

    /**
     * Creates the 'cytonic_chat' table in the database if it doesn't exist.
     * The table contains information about player chat messages.
     *
     * @throws IllegalStateException if the database connection is not open.
     */
    private void createChatTable() {
        worker.submit(() -> {
            if (isConnected()) {
                PreparedStatement ps;
                try {
                    ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonic_chat (id INT NOT NULL AUTO_INCREMENT, timestamp TIMESTAMP, uuid VARCHAR(36), message TEXT, PRIMARY KEY(id))");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occurred whilst fetching data from the database. Please report the following stacktrace to Foxikle:", e);
                }
            }
        });
    }

    /**
     * Creates the 'cytonic_ranks' table in the database if it doesn't exist.
     * The table contains information about player ranks.
     *
     * @throws IllegalStateException if the database connection is not open.
     */
    private void createRanksTable() {
        worker.submit(() -> {
            if (isConnected()) {
                PreparedStatement ps;
                try {
                    ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonic_ranks (uuid VARCHAR(36), rank_id VARCHAR(16), PRIMARY KEY(uuid))");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occurred whilst creating the `cytonic_ranks` table.", e);
                }
            }
        });
    }

    /**
     * Creates the 'cytonic_worlds' table in the database if it doesn't exist.
     * The table contains information about the worlds stored in the database.
     *
     * @throws IllegalStateException if the database connection is not open.
     */
    public void createWorldTable() {
        worker.submit(() -> {
            if (isConnected()) {
                PreparedStatement ps;
                try {
                    ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonic_worlds (world_name TEXT, world_type TEXT, last_modified TIMESTAMP, world_data MEDIUMBLOB, spawn_point TEXT, extra_data varchar(100))");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occurred whilst creating the `cytonic_worlds` table.", e);
                }
            }
        });
    }

    private void createPlayersTable() {
        worker.submit(() -> {
            if (isConnected()) {
                try (PreparedStatement ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonic_players (joined TIMESTAMP, uuid VARCHAR(36), ip TEXT)")) {
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occurred whilst creating the `cytonic_players` table.", e);
                }
            }
        });
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
        if (!isConnected())
            throw new IllegalStateException("The database must have an open connection to fetch a player's rank!");
        worker.submit(() -> {
            String id = "DEFAULT";
            try {
                PreparedStatement ps = connection.prepareStatement("SELECT rank_id FROM cytonic_ranks WHERE uuid = ?");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    id = rs.getString("rank_id");
                }
                future.complete(PlayerRank.valueOf(id));
            } catch (SQLException e) {
                Logger.error(STR."An error occurred whilst fetching the rank of '\{uuid}'");
            }
        });
        return future;
    }

    /**
     * Sets the given player's rank to the specified rank.
     *
     * @param uuid The player's UUID
     * @param rank The player's rank constant
     * @throws IllegalStateException if the database isn't connected
     */
    public CompletableFuture<Void> setPlayerRank(UUID uuid, PlayerRank rank) {
        if (!isConnected())
            throw new IllegalStateException("The database must have an open connection to set a player's rank!");
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO cytonic_ranks (uuid, rank_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE rank_id = VALUES(rank_id)");
                ps.setString(1, uuid.toString());
                ps.setString(2, rank.name());
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error(STR."An error occurred whilst setting the rank of '\{uuid}'");
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Adds a players chat message to the database.
     *
     * @param uuid    The player's UUID.
     * @param message The player's message.
     * @throws IllegalStateException if the database isn't connected.
     */
    public void addChat(UUID uuid, String message) {
        worker.submit(() -> {
            if (!isConnected())
                throw new IllegalStateException("The database must have an open connection to add a player's chat!");
            PreparedStatement ps;
            try {
                ps = connection.prepareStatement("INSERT INTO cytonic_chat (timestamp, uuid, message) VALUES (CURRENT_TIMESTAMP,?,?)");
                ps.setString(1, uuid.toString());
                ps.setString(2, message);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Adds a new world to the database.
     *
     * @param worldName  The name of the world to be added.
     * @param worldType  The type of the world.
     * @param world      The PolarWorld object representing the world.
     * @param spawnPoint The spawn point of the world.
     * @throws IllegalStateException If the database connection is not open.
     */
    public void addWorld(String worldName, String worldType, PolarWorld world, Pos spawnPoint) {
        if (!isConnected())
            throw new IllegalStateException("The database must have an open connection to add a world!");
        worker.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO cytonic_worlds (world_name, world_type, last_modified, world_data, spawn_point) VALUES (?,?, CURRENT_TIMESTAMP,?,?)");
                ps.setString(1, worldName);
                ps.setString(2, worldType);
                ps.setBytes(3, PolarWriter.write(world));
                ps.setString(4, PosSerializer.serialize(spawnPoint));
                ps.executeUpdate();
            } catch (SQLException e) {
                Logger.error("An error occurred whilst adding a world!", e);
            }
        });
    }

    /**
     * Retrieves a world from the database.
     *
     * @param worldName The name of the world to fetch.
     * @return A {@link CompletableFuture} that completes with the fetched {@link PolarWorld}.
     * If the world does not exist in the database, the future will complete exceptionally with a {@link RuntimeException}.
     * @throws IllegalStateException If the database connection is not open.
     */
    public CompletableFuture<PolarWorld> getWorld(String worldName) {
        CompletableFuture<PolarWorld> future = new CompletableFuture<>();
        if (!isConnected())
            throw new IllegalStateException("The database must have an open connection to fetch a world!");
        worker.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM cytonic_worlds WHERE world_name = ?")) {
                ps.setString(1, worldName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    PolarWorld world = PolarReader.read(rs.getBytes("world_data"));
                    CytosisSettings.SERVER_SPAWN_POS = PosSerializer.deserialize(rs.getString("spawn_point"));
                    future.complete(world);
                } else {
                    Logger.error("The result set is empty!");
                    throw new RuntimeException(STR."World not found: \{worldName}");
                }
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
 * <p>
 * This method uses a worker thread to execute the database operation.
 * It prepares a SQL statement to insert a new record into the 'cytonic_players' table.
 * The 'joined' column is set to the current timestamp, the 'uuid' column is set to the provided UUID,
 * and the 'ip' column is set to the provided IP address.
 * If an error occurs during the database operation, it is logged using the Logger.
 */
public void playerJoin(UUID uuid, SocketAddress ip) {
    worker.submit(() -> {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO cytonic_players (joined, uuid, ip) VALUES (CURRENT_TIMESTAMP,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, ip.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Failed to add a player to the database!", e);
        }
    });
}
}