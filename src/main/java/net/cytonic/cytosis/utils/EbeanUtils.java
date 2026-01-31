package net.cytonic.cytosis.utils;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;
import io.ebean.typequery.PInstant;
import io.ebean.typequery.QueryBean;
import lombok.experimental.UtilityClass;

import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.plugins.PluginContainer;
import net.cytonic.cytosis.plugins.PluginDescription;
import net.cytonic.cytosis.plugins.java.JavaPluginDescription;

@UtilityClass
public class EbeanUtils {

    public static void runPluginMigrations(PluginContainer container) {
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

    public static <T, R extends QueryBean<T, R>> void orderByInstant(PInstant<R> pBase, boolean orderBy) {
        if (orderBy) {
            pBase.asc();
            return;
        }
        pBase.desc();
    }
}
