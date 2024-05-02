package net.cytonic.cytosis.data;

import net.cytonic.cytosis.logging.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

public class Database {

    private final String host = "35.229.110.209";
    private final String port = "3306";
    private final String database = "cytonic-dev";
    private final String username = "cytonic";
    private final String password = "zmaI2zfdFVlAMzqUxkS1Up35ovv4raSaX9mYjWcCLM21mZWiYYcWGuKYOya9GKT7";
    private Connection connection;

    public Database() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.error("Failed to load database driver");
            Logger.error(e.toString());
        }
    }

    public boolean isConnected() {
        return (connection != null);
    }

    public void connect() throws ClassNotFoundException, SQLException {
        if (!isConnected())
            connection = DriverManager.getConnection(STR."jdbc:mysql://\{host}:\{port}/\{database}?useSSL=false&autoReconnect=true", username, password);
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
                Logger.info("Database connection closed!");
            } catch (SQLException e) {
                Logger.error(STR."""
An error occoured whilst disconnecting from the database. Please report the following stacktrace to Foxikle:\s
\{Arrays.toString(e.getStackTrace())}""");
            }
        }
    }

    private Connection getConnection() {
        return connection;
    }

    public void createChatTable() {
        if (isConnected()) {
            PreparedStatement ps;
            try {
                ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cytonicchat (id INT NOT NULL AUTO_INCREMENT, timestamp TIMESTAMP, uuid VARCHAR(36), message TEXT, PRIMARY KEY(id))");
                ps.executeUpdate();
            } catch (SQLException e) {
                Logger.error(STR."""
An error occoured whilst fetching data from the database. Please report the following stacktrace to Foxikle:\s
\{Arrays.toString(e.getStackTrace())}""");
            }
        }
    }

    public void addChat(UUID uuid, String message) {
        PreparedStatement ps;
        try {
            ps = connection.prepareStatement("INSERT INTO cytonicchat (timestamp,uuid,message) VALUES (CURRENT_TIMESTAMP,?,?)");
            ps.setString(1, uuid.toString());
            ps.setString(2,message);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}