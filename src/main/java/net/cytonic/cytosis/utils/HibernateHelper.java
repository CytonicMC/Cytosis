package net.cytonic.cytosis.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import net.minestom.server.MinecraftServer;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.JdbcSettings;
import org.hibernate.service.ServiceRegistry;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.EnvironmentDatabase;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.plugins.PluginManager;

@CytosisComponent(dependsOn = {EnvironmentDatabase.class, PluginManager.class})
public class HibernateHelper implements Bootstrappable {

    private static EntityManagerFactory factory;

    @Override
    public void init() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            Logger.error("Failed to load Postgres JDBC driver", e);
            MinecraftServer.stopCleanly();
        }

        Map<String, Object> settings = new HashMap<>();

        settings.put(AvailableSettings.JAKARTA_NON_JTA_DATASOURCE,
            Cytosis.get(EnvironmentDatabase.class).getDataSource());

        settings.put(JdbcSettings.SHOW_SQL, true);
        settings.put(JdbcSettings.FORMAT_SQL, true);
        settings.put(AvailableSettings.HBM2DDL_AUTO, "update");

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
            .applySettings(settings)
            .build();

        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        getClasses().forEach(metadataSources::addAnnotatedClass);

        Metadata metadata = metadataSources.buildMetadata();
        //noinspection resource
        factory = metadata.buildSessionFactory().unwrap(EntityManagerFactory.class);
    }

    @Override
    public void shutdown() {
        if (factory != null) {
            factory.close();
        }
    }

    public EntityManager createSession() {
        return factory.createEntityManager();
    }

    private List<Class<?>> getClasses() {
        List<Class<?>> candidates = new ArrayList<>();
        try (var scanResult = PluginManager.createClassGraph().scan()) {
            var classInfos = scanResult.getClassesWithAnnotation(Entity.class);
            for (var classInfo : classInfos) {
                try {
                    candidates.add(classInfo.loadClass());
                } catch (Throwable t) {
                    Logger.error("Failed to load annotated entity class " + classInfo.getName(), t);
                }
            }
        }
        return candidates;
    }
}