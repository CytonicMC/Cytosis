package net.cytonic.cytosis.data;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.ranks.PlayerRank;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
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
    }

    private Connection getConnection() {
        return connection;
    }

    private void createChatTable() {
        worker.submit(() -> {
            if (isConnected()) {
                PreparedStatement ps;
                try {
                    ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonic_chat (id INT NOT NULL AUTO_INCREMENT, timestamp TIMESTAMP, uuid VARCHAR(36), message TEXT, PRIMARY KEY(id))");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occurred whilst creating the `cytonic_chat` table.", e);
                }
            }
        });
    }

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

    public void createWorldTable() {
        worker.submit(() -> {
            if (isConnected()) {
                PreparedStatement ps;
                try {
                    ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonic_worlds (world_name TEXT, world_type TEXT, last_modified TIMESTAMP, world_data MEDIUMBLOB, x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, extra_data varchar(100))");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occurred whilst creating the `cytonic_worlds` table.", e);
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
     * Adds a players chat message to the database
     *
     * @param uuid    The player's UUID
     * @param message The player's message
     * @throws IllegalStateException if the database isn't connected
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

    public CompletableFuture<Void> addWorld(String worldName, String worldType, PolarWorld world, Pos spawnPoint) {
        if (!isConnected())
            throw new IllegalStateException("The database must have an open connection to add a world!");
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO cytonic_worlds (world_name, world_type, last_modified, world_data, x, y, z, yaw, pitch) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, worldName);
                ps.setString(2, worldType);
                ps.setBytes(3, PolarWriter.write(world));
                ps.setDouble(4, spawnPoint.x());
                ps.setDouble(5, spawnPoint.y());
                ps.setDouble(6, spawnPoint.z());
                ps.setFloat(7, spawnPoint.yaw());
                ps.setFloat(8, spawnPoint.pitch());
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst adding a world!",e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Gets a world
     *
     * @param worldName the world to fetch
     * @return The player's {@link PolarWorld}
     * @throws IllegalStateException if the database isn't connected
     */
    public CompletableFuture<PolarWorld> getWorld(String worldName) {
        CompletableFuture<PolarWorld> future = new CompletableFuture<>();
        if (!isConnected())
            throw new IllegalStateException("The database must have an open connection to fetch a world!");
        worker.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM cytonic_worlds WHERE world_name = ?");
                ps.setString(1, worldName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) future.complete(PolarReader.read(rs.getBytes("world_data")));
                else Logger.error("The result set is empty!");
                CytosisSettings.SERVER_SPAWN_POS = new Pos(rs.getDouble("x"),rs.getDouble("y"),rs.getDouble("z"), (float) rs.getDouble("yaw"), (float) rs.getDouble("pitch"));
            } catch (Exception e) {
                Logger.error("An error occurred whilst fetching a world!", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}