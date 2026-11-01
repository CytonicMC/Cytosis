package net.cytonic.cytosis.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.zaxxer.hikari.HikariDataSource;
import io.ebean.Database;
import io.ebean.DatabaseBuilder;
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

    @Override
    public void shutdown() {
        databases.forEach(Database::shutdown);
    }

    private void registerDatabase(String databaseName, HikariDataSource dataSource) {
        Properties props = new Properties();
        props.setProperty("ebean.migration.migrationPath", "dbmigration/cytosis/" + databaseName);
        props.setProperty("ebean.migration.run", "true");
        DatabaseBuilder builder = Database.builder()
            .dataSource(dataSource)
            .loadFromProperties(props)
            .shutdownHook(false)
            .runMigration(true)
            .defaultDatabase("environment".equals(databaseName))
            .name(databaseName);
        scanForEbeanClasses().forEach(builder::addClass);
        databases.add(builder.build());
        Logger.info("Successfully connected to the Ebean %s Database!", databaseName);
    }

    private List<Class<?>> scanForEbeanClasses() {
        List<Class<?>> classes = new ArrayList<>();
        classes.addAll(JandexUtils.getAnnotatedClassesClass(Entity.class));
        classes.addAll(JandexUtils.getAnnotatedClassesClass(Embeddable.class));
        return classes;
    }
}
