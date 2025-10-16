package net.cytonic.cytosis.plugins;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.managers.CommandDisablingManager;
import net.cytonic.cytosis.plugins.dependencies.DependencyUtils;
import net.cytonic.cytosis.plugins.dependencies.PluginDependency;
import net.cytonic.cytosis.plugins.loader.JavaPluginLoader;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles loading plugins and provides a registry for loaded plugins.
 */
@CytosisComponent(priority = 100, dependsOn = {CommandDisablingManager.class})
public class PluginManager implements Bootstrappable {

    private final Map<String, PluginContainer> pluginsById = new LinkedHashMap<>();
    private final Map<Object, PluginContainer> pluginInstances = new IdentityHashMap<>();
    private final Logger logger = LoggerFactory.getLogger("Plugin Manager");

    @Override
    public void init() {
        try {
            Path pluginsDirPath = Path.of("plugins");
            if (!Files.exists(pluginsDirPath)) {
                Files.createDirectories(pluginsDirPath);
                net.cytonic.cytosis.logging.Logger.info("Created plugins directory!");
            }

            loadPlugins(pluginsDirPath);
        } catch (Exception e) {
            net.cytonic.cytosis.logging.Logger.error("An error occurred whilst loading plugins!", e);
            throw new RuntimeException("An error occurred whilst loading plugins!", e);
        }
    }

    @Override
    public void shutdown() {
        unloadPlugins();
    }

    /**
     * Loads all plugins from the specified {@code directory}.
     *
     * @param directory the directory to load from
     * @throws IOException if we could not open the directory
     */
    public void loadPlugins(Path directory) throws IOException {
        checkNotNull(directory, "directory");
        checkArgument(directory.toFile().isDirectory(), "provided path isn't a directory");

        JavaPluginLoader loader = new JavaPluginLoader();
        Map<String, PluginDescription> foundCandidates = findCandidates(directory, loader);

        if (foundCandidates.isEmpty()) return;

        List<PluginDescription> sorted = DependencyUtils.sortCandidates(new ArrayList<>(foundCandidates.values()));

        Map<String, PluginDescription> loadedCandidates = new HashMap<>();
        Map<String, PluginContainer> pluginContainers = new LinkedHashMap<>();
        pluginLoad:
        for (PluginDescription candidate : sorted) {
            for (PluginDependency dependency : candidate.getDependencies()) {
                if (!dependency.isOptional() && !loadedCandidates.containsKey(dependency.getId())) {
                    logger.error("Can't load plugin {} due to missing dependency {}", candidate.getId(),
                        dependency.getId());
                    continue pluginLoad;
                }
            }

            try {
                PluginDescription realPlugin = loader.createPluginFromCandidate(candidate);
                PluginContainer container = new PluginContainer(realPlugin);
                pluginContainers.put(realPlugin.getId(), container);
                loadedCandidates.put(realPlugin.getId(), realPlugin);
            } catch (Throwable e) {
                logger.error("Can't create module for plugin {}", candidate.getId(), e);
            }
        }

        for (Map.Entry<String, PluginContainer> plugin : pluginContainers.entrySet()) {
            PluginContainer container = plugin.getValue();
            PluginDescription desc = container.getDescription();

            try {
                loader.createPlugin(container);
            } catch (Throwable e) {
                logger.error("Can't create plugin {}", desc.getId(), e);
                continue;
            }

            logger.info("Loaded plugin {} {}", desc.getId(), desc.getVersion().orElse("<UNKNOWN>"));
            registerPlugin(container);
        }
    }

    private Map<String, PluginDescription> findCandidates(Path directory, JavaPluginLoader loader) throws IOException {
        Map<String, PluginDescription> foundCandidates = new LinkedHashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory,
            p -> p.toFile().isFile() && p.toString().endsWith(".jar"))) {
            for (Path path : stream) {
                try {
                    PluginDescription candidate = loader.loadCandidate(path);

                    // If we found a duplicate candidate (with the same ID), don't load it.
                    PluginDescription maybeExistingCandidate = foundCandidates.putIfAbsent(candidate.getId(),
                        candidate);

                    if (maybeExistingCandidate != null) {
                        logger.error("Refusing to load plugin at path {} since we already "
                                + "loaded a plugin with the same ID {} from {}",
                            candidate.getSource().map(Objects::toString).orElse("<UNKNOWN>"), candidate.getId(),
                            maybeExistingCandidate.getSource().map(Objects::toString).orElse("<UNKNOWN>"));
                    }
                } catch (Throwable e) {
                    logger.error("Unable to load plugin {}", path, e);
                }
            }
        }
        return foundCandidates;
    }

    private void registerPlugin(PluginContainer plugin) {
        pluginsById.put(plugin.getDescription().getId(), plugin);
        Optional<CytosisPlugin> instance = plugin.getInstance();
        instance.ifPresent(o -> {
            pluginInstances.put(o, plugin);
            o.initialize();
        });
    }

    public Optional<PluginContainer> getPlugin(String id) {
        checkNotNull(id, "id");
        return Optional.ofNullable(pluginsById.get(id));
    }

    public Collection<PluginContainer> getPlugins() {
        return Collections.unmodifiableCollection(pluginsById.values());
    }

    public boolean isLoaded(String id) {
        return pluginsById.containsKey(id);
    }

    public void addToClasspath(Object plugin, Path path) {
        checkNotNull(plugin, "instance");
        checkNotNull(path, "path");
        Optional<PluginContainer> optContainer = fromInstance(plugin);
        checkArgument(optContainer.isPresent(), "plugin is not loaded");
        Optional<?> optInstance = optContainer.get().getInstance();
        checkArgument(optInstance.isPresent(), "plugin has no instance");

        ClassLoader pluginClassloader = optInstance.get().getClass().getClassLoader();
        if (pluginClassloader instanceof PluginClassLoader) {
            ((PluginClassLoader) pluginClassloader).addPath(path);
        } else {
            throw new UnsupportedOperationException("Operation is not supported on non-Java Cytosis plugins.");
        }
    }

    public Optional<PluginContainer> fromInstance(Object instance) {
        checkNotNull(instance, "instance");

        if (instance instanceof PluginContainer) {
            return Optional.of((PluginContainer) instance);
        }

        return Optional.ofNullable(pluginInstances.get(instance));
    }

    public void unloadPlugins() {
        for (PluginContainer value : pluginsById.values()) {
            value.getInstance().ifPresent(CytosisPlugin::shutdown);
        }
    }
}
