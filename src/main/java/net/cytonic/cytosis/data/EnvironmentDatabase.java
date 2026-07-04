package net.cytonic.cytosis.data;

import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minestom.server.MinecraftServer;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisConfig;
import net.cytonic.cytosis.config.CytosisConfig.DatabaseConfig;
import net.cytonic.cytosis.environments.Environment;
import net.cytonic.cytosis.logging.Logger;

/**
 * A class handling Cytosis database transactions within its environment. Things like player ranks, punishments, and
 * some worlds are stored in {@link GlobalDatabase}.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
@CytosisComponent
public class EnvironmentDatabase implements Bootstrappable {

    private final ExecutorService worker;
    @Getter
    private HikariDataSource dataSource;

    /**
     * Creates and initializes a new EnvironmentDatabase
     */
    public EnvironmentDatabase() {
        String prefix = Cytosis.get(Environment.class).getPrefix();
        this.worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisDatabaseWorker")
            .uncaughtExceptionHandler(
                (t, e) -> Logger.error("An uncaught exception occurred on the database worker thread: " + t.getName(),
                    e)).factory());
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
            HikariConfig config = GlobalDatabase.getHikariConfig();
            DatabaseConfig settings = Cytosis.get(CytosisConfig.class).database();
            String prefix = Cytosis.get(Environment.class).getPrefix();
            config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s",
                settings.host(),
                settings.port(),
                prefix + settings.database()));
            this.dataSource = new HikariDataSource(config);
            try {
                // Test the connection
                try (Connection conn = dataSource.getConnection()) {
                    Logger.info("Successfully connected to the Environmental Database!");
                }
            } catch (Throwable e) {
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
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Creates the database tables
     */
    public void createTables() {
        createPlayerJoinsTable();
    }

    /**
     * Create the player join logging table
     */
    private void createPlayerJoinsTable() {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS cytonic_player_joins (joined TIMESTAMP, uuid UUID, ip TEXT)");
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error("An error occurred whilst creating the `cytonic_player_joins` table.", e);
        }
    }

    /**
     * Logs a player's join event to the database.
     *
     * @param uuid The unique identifier of the player.
     * @param ip   The IP address of the player.
     */
    public void logPlayerJoin(UUID uuid, SocketAddress ip) {
        checkConditions();

        worker.submit(() -> {
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO cytonic_player_joins (joined, uuid, ip) VALUES (CURRENT_TIMESTAMP,?,?)");
                ps.setObject(1, uuid);
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
        checkConditions();

        CompletableFuture<ResultSet> future = new CompletableFuture<>();
        worker.submit(() -> {
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
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
        checkConditions();

        CompletableFuture<ResultSet> future = new CompletableFuture<>();
        worker.submit(() -> {
            try (Connection conn = getConnection()) {
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
        checkConditions();
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            return ps.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet querySync(PreparedStatement ps) {
        checkConditions();
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
        checkConditions();
        return getConnection().prepareStatement(sql);
    }

    /**
     * Updates the database with the specified SQL
     *
     * @param sql The SQL update
     * @return A {@link CompletableFuture} for when the update is completed
     */
    public CompletableFuture<Void> update(PreparedStatement sql) {
        checkConditions();

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
        checkConditions();

        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                Logger.error("An error occurred whilst updating the database!", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private void checkConditions() {
        if (!isConnected()) {
            throw new IllegalStateException("The database must have an open connection to make a query!");
        }
    }
}