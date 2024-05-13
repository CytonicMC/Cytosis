package net.cytonic.cytosis.data;

import lombok.Getter;

@Getter
public class DatabaseManager {

    private Database database;

    public DatabaseManager() {
    }

    public void shutdown() {
        database.disconnect();
    }

    public void setupDatabase() {
        database = new Database();
        database.connect();
        database.createTables();
    }
}