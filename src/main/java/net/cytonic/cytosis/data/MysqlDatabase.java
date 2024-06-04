package net.cytonic.cytosis.data;

import net.cytonic.cytosis.auditlog.Category;
import net.cytonic.cytosis.auditlog.Entry;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.ranks.PlayerRank;
import net.cytonic.cytosis.utils.BanData;
import net.cytonic.cytosis.utils.PosSerializer;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MysqlDatabase {

    private final ExecutorService worker;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean ssl;
    private Connection connection;

    public MysqlDatabase() {
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
        createBansTable();
        createAuditLogTable();
        createPlayersTable();
        createWorldTable();
        createPlayerJoinsTable();
        createChatChannelsTable();
    }

    private Connection getConnection() {
        return connection;
    }

    private void createChatTable() {
        worker.submit(() -> {
            if (isConnected()) {
                PreparedStatement ps;
                try {
                    ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonicchat (id INT NOT NULL AUTO_INCREMENT, timestamp TIMESTAMP, uuid VARCHAR(36), message TEXT, PRIMARY KEY(id))");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occoured whilst fetching data from the database. Please report the following stacktrace to Foxikle:", e);
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
                    Logger.error("An error occoured whilst creating the `cytonic_ranks` table.", e);
                }
            }
        });
    }

    private void createBansTable() {
        worker.submit(() -> {
            if (isConnected()) {
                PreparedStatement ps;
                try {
                    ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonic_bans (uuid VARCHAR(36), to_expire VARCHAR(100), reason TINYTEXT, PRIMARY KEY(uuid))");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occoured whilst creating the `cytonic_bans` table.", e);
                }
            }
        });
    }

    private void createPlayersTable() {
        worker.submit(() -> {
            if (isConnected()) {
                PreparedStatement ps;
                try {
                    ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonic_players (uuid VARCHAR(36), name VARCHAR(16), PRIMARY KEY(uuid))");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occoured whilst creating the `cytonic_players` table.", e);
                }
            }
        });
    }

    /**
     * actor is staff<p>
     * category would be BAN, see {@link Category}<p>
     * uuid is the player<p>
     * id and timestamp are handled by mysql<p>
     */
    private void createAuditLogTable() {
        worker.submit(() -> {
            if (isConnected()) {
                PreparedStatement ps;
                try {
                    ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonic_audit_log (id INT NOT NULL AUTO_INCREMENT, timestamp TIMESTAMP, uuid VARCHAR(36), reason TINYTEXT, category VARCHAR(50), actor VARCHAR(36), PRIMARY KEY(id))");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occoured whilst fetching data from the database. Please report the following stacktrace to Foxikle:", e);
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
            try {
                PreparedStatement ps = connection.prepareStatement("SELECT rank_id FROM cytonic_ranks WHERE uuid = ?");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    future.complete(PlayerRank.valueOf(rs.getString("rank_id")));
                } else {
                    future.complete(PlayerRank.DEFAULT);
                    setPlayerRank(uuid, PlayerRank.DEFAULT);
                }
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
            }
        });
        return future;
    }

    public void addChat(UUID uuid, String message) {
        worker.submit(() -> {
            PreparedStatement ps;
            try {
                ps = connection.prepareStatement("INSERT INTO cytonicchat (timestamp, uuid, message) VALUES (CURRENT_TIMESTAMP,?,?)");
                ps.setString(1, uuid.toString());
                ps.setString(2, message);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> addAuditLogEntry(Entry entry) {
        if (!isConnected()) throw new IllegalStateException("The database must be connected to add an auditlog entry.");
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            PreparedStatement ps;
            try {
                ps = connection.prepareStatement("INSERT INTO cytonic_audit_log (timestamp, uuid, reason, category, actor) VALUES (CURRENT_TIMESTAMP,?,?,?,?)");
                ps.setString(1, entry.uuid().toString());
                ps.setString(2, entry.reason());
                ps.setString(3, entry.category().name());
                ps.setString(4, entry.actor().toString());
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst adding an auditlog entry!", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public CompletableFuture<Void> banPlayer(UUID uuid, String reason, Instant toExpire) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        worker.submit(() -> {
            if (!isConnected()) throw new IllegalStateException("The database must be connected to ban players.");
            try {
                PreparedStatement ps = getConnection().prepareStatement("INSERT IGNORE INTO cytonic_bans (uuid, to_expire, reason) VALUES (?,?,?)");
                ps.setString(1, uuid.toString());
                ps.setString(2, toExpire.toString());
                ps.setString(3, reason);
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error(STR."An error occurred whilst banning the player \{uuid}.", e);
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
        if (!isConnected()) throw new IllegalStateException("The database must be connected.");
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
                            Logger.error(STR."An error occurred whilst determining if the player \{uuid} is banned.", e);
                            future.complete(new BanData(null, null, true));
                        }
                    }
                } else {
                    future.complete(new BanData(null, null, false));
                }
            } catch (SQLException e) {
                Logger.error(STR."An error occurred whilst determining if the player \{uuid} is banned.", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public CompletableFuture<UUID> findUUIDByName(String name) {
        if (!isConnected()) throw new IllegalStateException("The database must be connected.");
        CompletableFuture<UUID> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM cytonic_players WHERE name = ?");
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    future.complete(UUID.fromString(rs.getString("uuid")));
                } else {
                    future.completeExceptionally(new IllegalArgumentException(STR."The player '\{name}' doesn't exist!"));
                }
            } catch (SQLException e) {
                Logger.error(STR."An error occurred whilst determining \{name}'s UUID.", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public CompletableFuture<Void> addPlayer(Player player) {
        if (!isConnected()) throw new IllegalStateException("The database must be connected.");
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("INSERT IGNORE INTO cytonic_players (name, uuid) VALUES (?,?) ON DUPLICATE KEY UPDATE name = ?");
                ps.setString(1, player.getUsername());
                ps.setString(2, player.getUuid().toString());
                ps.setString(3, player.getUsername());
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error(STR."An error occurred whilst setting the name of \{player.getUuid().toString()}.", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public CompletableFuture<Void> unbanPlayer(UUID uuid) {
        if (!isConnected()) throw new IllegalStateException("The database must be connected.");
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("DELETE FROM cytonic_bans WHERE uuid = ?");
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error(STR."An error occurred whilst unbanning the player \{uuid}.", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

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

    /**
     * Creates the 'cytonic_chat_channels' table in the database if it doesn't exist.
     * The table contains information about player's chat channels.
     *
     * @throws IllegalStateException if the database connection is not open.
     */
    private void createChatChannelsTable() {
        worker.submit(() -> {
            if (isConnected()) {
                PreparedStatement ps;
                try {
                    ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonic_chat_channels (uuid VARCHAR(36), chat_channel VARCHAR(16), PRIMARY KEY(uuid))");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occurred whilst creating the `cytonic_chat_channels` table.", e);
                }
            }
        });
    }

    private void createPlayerJoinsTable() {
        worker.submit(() -> {
            if (isConnected()) {
                try (PreparedStatement ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonic_player_joins (joined TIMESTAMP, uuid VARCHAR(36), ip TEXT)")) {
                    ps.executeUpdate();
                } catch (SQLException e) {
                    Logger.error("An error occurred whilst creating the `cytonic_player_joins` table.", e);
                }
            }
        });
    }

    public void setChatChannel(UUID uuid, ChatChannel chatChannel) {
        worker.submit(() -> {
            if (!isConnected())
                throw new IllegalStateException("The database must have an open connection to add a player's chat!");
            PreparedStatement ps;
            try {
                ps = connection.prepareStatement("INSERT INTO cytonic_chat_channels (uuid, chat_channel) VALUES (?, ?) ON DUPLICATE KEY UPDATE chat_channel = VALUES(chat_channel)");
                ps.setString(1, uuid.toString());
                ps.setString(2, chatChannel.name());
                ps.executeUpdate();
            } catch (SQLException e) {
                Logger.error("An error occurred whilst setting a players chat channel.", e);
            }
        });
    }

    public CompletableFuture<ChatChannel> getChatChannel(@NotNull final UUID uuid) {
        CompletableFuture<ChatChannel> future = new CompletableFuture<>();
        if (!isConnected())
            throw new IllegalStateException("The database must have an open connection to fetch a player's chat channel!");
        worker.submit(() -> {
            String channel = "ALL";
            try {
                PreparedStatement ps = connection.prepareStatement("SELECT chat_channel FROM cytonic_chat_channels WHERE uuid = ?");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    channel = rs.getString("chat_channel");
                }
                future.complete(ChatChannel.valueOf(channel));
            } catch (SQLException e) {
                Logger.error(STR."An error occurred whilst fetching the chat channel of '\{uuid}'", e);
            }
        });
        return future;
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
     *             <p>
     *             This method uses a worker thread to execute the database operation.
     *             It prepares a SQL statement to insert a new record into the 'cytonic_player_joins' table.
     *             The 'joined' column is set to the current timestamp, the 'uuid' column is set to the provided UUID,
     *             and the 'ip' column is set to the provided IP address.
     *             If an error occurs during the database operation, it is logged using the Logger.
     */
    public void logPlayerJoin(UUID uuid, SocketAddress ip) {
        worker.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO cytonic_player_joins (joined, uuid, ip) VALUES (CURRENT_TIMESTAMP,?,?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, ip.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                Logger.error("Failed to add a player to the database!", e);
            }
        });
    }
}