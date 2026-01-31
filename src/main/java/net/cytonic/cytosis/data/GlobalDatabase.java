package net.cytonic.cytosis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.BanData;
import net.cytonic.cytosis.data.objects.preferences.PreferenceData;
import net.cytonic.cytosis.environments.EnvironmentManager;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.PosSerializer;
import net.cytonic.cytosis.utils.Utils;

/**
 * The database object that handles data that is stored across network environments
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
@CytosisComponent(dependsOn = {FileManager.class, EnvironmentManager.class})
public class GlobalDatabase implements Bootstrappable {

    private final ExecutorService worker;
    private HikariDataSource dataSource;

    /**
     * Creates and initializes a new Global Database
     */
    public GlobalDatabase() {
        this.worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisDatabaseWorker")
            .uncaughtExceptionHandler(
                (t, e) -> Logger.error("An uncaught exception occurred on the thread: " + t.getName(), e)).factory());
    }

    protected static @NonNull HikariConfig getHikariConfig() {
        CytosisSettings settings = Cytosis.get(CytosisSettings.class);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s",
            settings.getDatabaseConfig().getHost(),
            settings.getDatabaseConfig().getPort(),
            settings.getDatabaseConfig().getGlobalDatabase()));
        config.setUsername(settings.getDatabaseConfig().getUser());
        config.setPassword(settings.getDatabaseConfig().getPassword());

        // HikariCP optimizations
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10); // Adjust depending on our needs
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setPoolName("CytosisPool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        return config;
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
                HikariConfig config = getHikariConfig();
                this.dataSource = new HikariDataSource(config);

                // Test the connection
                try (Connection conn = dataSource.getConnection()) {
                    Logger.info("Successfully connected to the Global Database!");
                }
                registerDatabase("global", dataSource);
            } catch (SQLException e) {
                Logger.error("Invalid Database Credentials!", e);
                MinecraftServer.stopCleanly();
            }
        }
    }

    protected static void registerDatabase(String databaseName, HikariDataSource dataSource) {
        Properties props = new Properties();
        props.setProperty("ebean.migration.migrationPath", "dbmigration/cytosis/" + databaseName);
        props.setProperty("ebean.migration.run", "true");
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setDataSource(dataSource);
        databaseConfig.loadFromProperties(props);
        databaseConfig.runMigration(true);
        databaseConfig.defaultDatabase("environment".equals(databaseName));
        databaseConfig.name(databaseName);
        DatabaseFactory.create(databaseConfig);
        Logger.info("Successfully connected to the Ebean " + databaseName + " Database!");
    }

    /**
     * Checks if the database is connected
     *
     * @return if the database is connected
     */
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Disconnects from the database server (closes the pool)
     */
    public void disconnect() {
        if (isConnected()) {
            dataSource.close();
            Logger.info("Database connection pool closed!");
        }
    }

    /**
     * Gets a connection from the pool
     *
     * @return A connection from the pool
     * @throws SQLException if a connection cannot be obtained
     */
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
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

        createOptimizedIndexes();
    }

    private void createOptimizedIndexes() {
        String[] indexes = {
            "CREATE INDEX IF NOT EXISTS idx_bans_expiry ON cytonic_bans(to_expire)",
            "CREATE INDEX IF NOT EXISTS idx_mutes_expiry ON cytonic_mutes(to_expire)",
            "CREATE INDEX IF NOT EXISTS idx_players_name ON cytonic_players(name)",
        };

        for (String sql : indexes) {
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.execute();
            } catch (Exception e) {
                Logger.error("Failed to create index", e);
            }
        }
    }

    /**
     * Creates the bans table
     */
    private void createBansTable() {
        try (Connection conn = getConnection()) {
            conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_bans (
                    uuid UUID,
                    to_expire VARCHAR(100),
                    reason TEXT,
                    PRIMARY KEY(uuid)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the \"cytonic_bans\" table.", e);
        }
    }

    /**
     * Creates the player data table
     */
    private void createPlayersTable() {
        try (Connection conn = getConnection()) {
            conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_players (
                    uuid UUID,
                    "rank" VARCHAR(16),
                    name VARCHAR(16),
                    PRIMARY KEY(uuid)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the \"cytonic_players\" table.", e);
        }
    }

    /**
     * Creates the world table
     */
    public void createWorldTable() {
        try (Connection conn = getConnection()) {
            conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_worlds (
                    world_name VARCHAR(255),
                    world_type TEXT,
                    last_modified TIMESTAMP,
                    world_data BYTEA,
                    spawn_point TEXT,
                    extra_data TEXT,
                    uuid UUID,
                    PRIMARY KEY(world_name)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the \"cytonic_worlds\" table.", e);
        }
    }

    public void createPreferencesTable() {
        try (Connection conn = getConnection()) {
            conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_preferences (
                    uuid UUID PRIMARY KEY,
                    preferences TEXT
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the \"cytonic_preferences\" table.", e);
        }
    }

    public void createFriendTable() {
        try (Connection conn = getConnection()) {
            conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_friends (
                    uuid UUID,
                    friends TEXT,
                    PRIMARY KEY (uuid)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the \"cytonic_friends\" table.", e);
        }
    }

    /**
     * Creates the bans table
     */
    private void createMutesTable() {
        try (Connection conn = getConnection()) {
            conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS cytonic_mutes (
                    uuid UUID,
                    to_expire VARCHAR(100),
                    PRIMARY KEY(uuid)
                )
                """).executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the \"cytonic_mutes\" table.", e);
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
            try (Connection conn = getConnection()) {
                Cytosis.get(CytonicNetwork.class).getMutedPlayers().put(uuid, true);
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO cytonic_mutes (uuid, to_expire) VALUES (?,?) ON CONFLICT (uuid) DO NOTHING");
                ps.setObject(1, uuid);
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
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM cytonic_mutes WHERE uuid = ?");
                ps.setObject(1, uuid);
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
            try (Connection conn = getConnection()) {
                Cytosis.get(CytonicNetwork.class).getMutedPlayers().remove(uuid);
                PreparedStatement ps = conn.prepareStatement("DELETE FROM cytonic_mutes WHERE uuid = ?");
                ps.setObject(1, uuid);
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
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT \"rank\" FROM cytonic_players WHERE uuid = ?");
                ps.setObject(1, uuid);
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
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO cytonic_players (uuid, "rank")
                    VALUES (?,?)
                    ON CONFLICT (uuid) DO UPDATE SET "rank" = EXCLUDED.rank
                    """);
                ps.setObject(1, uuid);
                ps.setString(2, rank.name());
                ps.executeUpdate();

                Cytosis.get(RedisDatabase.class)
                    .addToGlobalHash("player_ranks", uuid.toString(), rank.name());

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
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO cytonic_bans (uuid, to_expire, reason) VALUES (?,?,?) ON CONFLICT (uuid) DO NOTHING");
                ps.setObject(1, uuid);
                ps.setString(2, toExpire.toString());
                ps.setString(3, reason);
                ps.executeUpdate();

                RedisDatabase redis = Cytosis.get(RedisDatabase.class);
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
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM cytonic_bans WHERE uuid = ?");
                ps.setObject(1, uuid);
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
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM cytonic_bans WHERE uuid = ?");
                ps.setObject(1, uuid);
                ps.executeUpdate();

                RedisDatabase redis = Cytosis.get(RedisDatabase.class);
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
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM cytonic_players WHERE name = ?");
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
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO cytonic_players (name, uuid, "rank")
                    VALUES (?,?,?) ON CONFLICT (uuid) DO UPDATE SET name = EXCLUDED.name
                    """);
                ps.setString(1, player.getTrueUsername());
                ps.setObject(2, player.getUuid());
                ps.setString(3, player.getRank().name());
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
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO cytonic_worlds (world_name, world_type, last_modified, world_data, spawn_point, uuid)
                    VALUES (?,?, CURRENT_TIMESTAMP,?,?,?)
                    """);
                ps.setString(1, worldName);
                ps.setString(2, worldType);
                ps.setBytes(3, PolarWriter.write(world));
                ps.setString(4, PosSerializer.serialize(spawnPoint));
                ps.setObject(5, worldUuid);
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
            try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM cytonic_worlds WHERE world_name = ?")) {
                ps.setString(1, worldName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    PolarWorld world = PolarReader.read(rs.getBytes("world_data"));
                    Cytosis.get(CytosisSettings.class)
                        .getServerConfig().setSpawnPos(PosSerializer.deserialize(rs.getString("spawn_point")));
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
            try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM cytonic_worlds WHERE world_name = ? AND world_type = ?")) {
                ps.setString(1, worldName);
                ps.setString(2, worldType);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    PolarWorld world = PolarReader.read(rs.getBytes("world_data"));
                    Cytosis.get(CytosisSettings.class)
                        .getServerConfig().setSpawnPos(PosSerializer.deserialize(rs.getString("spawn_point")));
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
            try (PreparedStatement ps = getConnection().prepareStatement(
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
            try (PreparedStatement ps = getConnection().prepareStatement(
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

    public List<PlayerEntry> loadPlayers() {
        List<PlayerEntry> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            ResultSet rs = conn.prepareStatement("""
                SELECT
                    p.uuid,
                    p.name,
                    p.rank,
                    b.reason as ban_reason,
                    b.to_expire as ban_expiry,
                    m.to_expire as mute_expiry
                FROM cytonic_players p
                LEFT JOIN cytonic_bans b ON p.uuid = b.uuid
                LEFT JOIN cytonic_mutes m ON p.uuid = m.uuid
                """).executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String name = rs.getString("name");
                PlayerRank rank = PlayerRank.valueOf(rs.getString("rank"));

                // Ban data (if exists)
                BanData banData = null;
                String banReason = rs.getString("ban_reason");
                if (banReason != null) {
                    Instant banExpiry = Instant.parse(rs.getString("ban_expiry"));
                    banData = new BanData(banReason, banExpiry, true);
                }

                Instant muteExpiry = null;
                if (rs.getString("mute_expiry") != null) {
                    muteExpiry = Instant.parse(rs.getString("mute_expiry"));
                }

                list.add(new PlayerEntry(uuid, name, rank, banData, muteExpiry));
            }
        } catch (SQLException e) {
            Logger.error("An error occurred whilst loading players!", e);
        }
        return list;
    }

    public CompletableFuture<@Nullable PreferenceData> loadPlayerPreferences(UUID player) {
        CompletableFuture<PreferenceData> future = new CompletableFuture<>();
        worker.submit(() -> {
            try (Connection conn = getConnection()) {
                PreparedStatement load = conn.prepareStatement(
                    "SELECT * FROM cytonic_preferences WHERE uuid = ?");
                load.setObject(1, player);
                ResultSet rs = load.executeQuery();
                if (rs.next()) {
                    PreferenceData data = PreferenceData.deserialize(rs.getString("preferences"));
                    future.complete(data);
                } else {
                    future.complete(null);
                }
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    public CompletableFuture<Set<UUID>> loadFriends(UUID player) {
        CompletableFuture<Set<UUID>> future = new CompletableFuture<>();
        worker.submit(() -> {
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM cytonic_friends WHERE uuid = ?");
                ps.setObject(1, player);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    future.complete(Cytosis.GSON.fromJson(rs.getString("friends"), Utils.UUID_SET));
                } else {
                    future.complete(new HashSet<>());
                }
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    public void updateFriends(UUID player, Set<UUID> friends) {
        worker.submit(() -> {

            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO cytonic_friends (uuid, friends)
                    VALUES (?, ?) ON CONFLICT (uuid) DO UPDATE SET friends = EXCLUDED.friends
                    """);
                friends.remove(player); // prevent self as a friend somehow
                String serialized = Cytosis.GSON.toJson(friends, Utils.UUID_SET);
                ps.setObject(1, player);
                ps.setString(2, serialized);
                ps.executeUpdate();
            } catch (Exception e) {
                Logger.error("Failed to persist friend data!", e);
            }
        });
    }

    public void addNewPlayerPreferences(UUID player, PreferenceData data) {
        worker.submit(() -> {
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO cytonic_preferences VALUES(?,?)");
                ps.setObject(1, player);
                ps.setString(2, data.serialize());
                ps.executeUpdate();
            } catch (Exception e) {
                Logger.error("An error occurred whilst adding a new player's preferences!", e);
            }
        });
    }

    public void persistPlayerPreferences(UUID player, PreferenceData data) {
        worker.submit(() -> {
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE cytonic_preferences SET preferences = ? WHERE uuid = ?");
                ps.setString(1, data.serialize());
                ps.setObject(2, player);
                ps.executeUpdate();
            } catch (Exception e) {
                Logger.error("An error occurred whilst adding a new player's preferences!", e);
            }
        });
    }

    public record PlayerEntry(UUID uuid, String username, PlayerRank rank, @Nullable BanData banData,
                              @Nullable Instant muteExpiry) {

    }
}
