package net.cytonic.cytosis.data;

import net.cytonic.cytosis.Cytosis;

import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

public final class DatabaseTemplate {
    private DatabaseTemplate() {
    }

    @SuppressWarnings("preview")
    public static final StringTemplate.Processor<CompletableFuture<ResultSet>, RuntimeException> QUERY = stringTemplate -> Cytosis.getDatabaseManager().getMysqlDatabase().query(STR.process(stringTemplate));

    @SuppressWarnings("preview")
    public static final StringTemplate.Processor<CompletableFuture<Void>, RuntimeException> UPDATE = stringTemplate -> Cytosis.getDatabaseManager().getMysqlDatabase().update(STR.process(stringTemplate));

}
