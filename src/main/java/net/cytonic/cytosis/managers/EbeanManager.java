package net.cytonic.cytosis.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.zaxxer.hikari.HikariDataSource;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisBootstrap;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.EnvironmentDatabase;
import net.cytonic.cytosis.data.GlobalDatabase;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.plugins.PluginContainer;
import net.cytonic.cytosis.plugins.PluginDescription;
import net.cytonic.cytosis.plugins.PluginManager;
import net.cytonic.cytosis.plugins.java.JavaPluginDescription;

@CytosisComponent(dependsOn = {PluginManager.class, EnvironmentDatabase.class, GlobalDatabase.class})
public class EbeanManager implements Bootstrappable {

    @Override
    public void init() {
        registerDatabase("global", Cytosis.get(GlobalDatabase.class).getDataSource());
        registerDatabase("environment", Cytosis.get(EnvironmentDatabase.class).getDataSource());

        Cytosis.get(PluginManager.class).getPlugins().forEach(this::runPluginMigrations);

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
        databaseConfig.runMigration(true);
        databaseConfig.defaultDatabase("environment".equals(databaseName));
        databaseConfig.name(databaseName);
        DatabaseFactory.create(databaseConfig);
        Logger.info("Successfully connected to the Ebean " + databaseName
            + " Database!");
    }

    private List<Class<?>> scanForEbeanClasses() {
        ClassGraph graph = new ClassGraph()
            .acceptPackages(CytosisBootstrap.SCAN_PACKAGE_ROOT)
            .enableClassInfo()
            .enableAnnotationInfo()
            .overrideClassLoaders(PluginManager.getClassLoaders());
        try (ScanResult scanResult = graph.scan()) {
            List<Class<?>> classes = new ArrayList<>();

            // Scan for @Entity classes
            classes.addAll(scanResult.getClassesWithAnnotation(Entity.class).loadClasses());

            // Scan for @Embeddable classes
            classes.addAll(scanResult.getClassesWithAnnotation(Embeddable.class).loadClasses());

            return classes;
        }
    }

    public void runPluginMigrations(PluginContainer container) {
        PluginDescription desc = container.getDescription();
        try {
            if (!(desc instanceof JavaPluginDescription javaDesc)) {
                Logger.warn("Cannot run migrations for non-Java plugin %s", desc.getId());
                return;
            }

            ClassLoader pluginClassLoader = javaDesc.getMainClass().getClassLoader();

            runMigrationsForDatabase("global", pluginClassLoader, javaDesc.getId());
            runMigrationsForDatabase("environment", pluginClassLoader, javaDesc.getId());

        } catch (Exception e) {
            Logger.error("Failed to run migrations for plugin %s", desc.getId(), e);
        }
    }

    private void runMigrationsForDatabase(String databaseName, ClassLoader pluginClassLoader, String pluginId) {
        try {
            Database database = DB.byName(databaseName);
            MigrationConfig migrationConfig = new MigrationConfig();
            migrationConfig.setMetaTable(
                "db_migration_" + pluginId.replace("-", "_"));
            migrationConfig.setMigrationPath("dbmigration/" + pluginId + "/" + databaseName);
            migrationConfig.setClassLoader(pluginClassLoader);
            String dbSchema = database.pluginApi().config().getDbSchema();
            if (dbSchema != null) {
                migrationConfig.setDbSchema(dbSchema);
            }

            MigrationRunner runner = new MigrationRunner(migrationConfig);
            runner.run(database.dataSource());
        } catch (Exception e) {
            Logger.error("Failed to run migrations for plugin '%s' on database '%s'",
                pluginId, databaseName, e);
        }
    }
}
