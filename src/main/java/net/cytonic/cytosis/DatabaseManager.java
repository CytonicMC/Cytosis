package net.cytonic.cytosis;

import lombok.Getter;
import net.cytonic.cytosis.data.Database;

@Getter
public class DatabaseManager {

    private Database database;

    public void shutdown() {
        database.disconnect();
    }

    public void setupDatabase() {
        database = new Database();
        database.connect();
        database.createTables();
    }
}