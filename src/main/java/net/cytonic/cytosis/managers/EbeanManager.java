package net.cytonic.cytosis.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.zaxxer.hikari.HikariDataSource;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.EnvironmentDatabase;
import net.cytonic.cytosis.data.GlobalDatabase;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.protocol.utils.JandexUtils;

@CytosisComponent(dependsOn = {EnvironmentDatabase.class, GlobalDatabase.class})
public class EbeanManager implements Bootstrappable {

    private final List<Database> databases = new ArrayList<>();

    @Override
    public void init() {
        registerDatabase("global", Cytosis.get(GlobalDatabase.class).getDataSource());
        registerDatabase("environment", Cytosis.get(EnvironmentDatabase.class).getDataSource());

        Logger.info("Errors will now be broadcast through snooper.");
        Cytosis.CONTEXT.setSendErrorsThroughSnooper(true);
    }

    private void registerDatabase(String databaseName, HikariDataSource dataSource) {
        Properties props = new Properties();
        props.setProperty("ebean.migration.migrationPath", "dbmigration/cytosis/" + databaseName);
        props.setProperty("ebean.migration.run", "true");
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setDataSource(dataSource);
        scanForEbeanClasses().forEach(databaseConfig::addClass);
        databaseConfig.loadFromProperties(props);
        databaseConfig.shutdownHook(false);
        databaseConfig.runMigration(true);
        databaseConfig.defaultDatabase("environment".equals(databaseName));
        databaseConfig.name(databaseName);
        databases.add(DatabaseFactory.create(databaseConfig));
        Logger.info("Successfully connected to the Ebean " + databaseName
            + " Database!");
    }

    private List<Class<?>> scanForEbeanClasses() {
        List<Class<?>> classes = new ArrayList<>();
        JandexUtils.getAnnotatedClasses(Entity.class).forEach(it -> classes.add(it.getClass()));
        JandexUtils.getAnnotatedClasses(Embeddable.class).forEach(it -> classes.add(it.getClass()));
        return classes;
    }

    @Override
    public void shutdown() {
        databases.forEach(Database::shutdown);
    }
}
