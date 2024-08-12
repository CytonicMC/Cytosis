package net.cytonic.cytosis.data;

import net.cytonic.cytosis.Cytosis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * A class providing utility StringTemplates for the database
 */
@SuppressWarnings("preview")
public final class DatabaseTemplate {
    private DatabaseTemplate() {
    }


    /**
     * Queries the database with the specified SQL string
     */
    public static final StringTemplate.Processor<CompletableFuture<ResultSet>, RuntimeException> QUERY = stringTemplate -> Cytosis.getDatabaseManager().getMysqlDatabase().query(STR.process(stringTemplate));

    /**
     * Updates the database with the specified SQL string
     */
    public static final StringTemplate.Processor<CompletableFuture<Void>, RuntimeException> UPDATE = stringTemplate -> Cytosis.getDatabaseManager().getMysqlDatabase().update(STR.process(stringTemplate));

    /**
     * Prepares a statement
     */
    public static final StringTemplate.Processor<PreparedStatement, SQLException> PREPARE = stringTemplate -> Cytosis.getDatabaseManager().getMysqlDatabase().prepareStatement(STR.process(stringTemplate));
}
