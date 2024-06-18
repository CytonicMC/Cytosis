package net.cytonic.cytosis.plugins.loader;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.plugins.CytosisPlugin;
import net.cytonic.cytosis.plugins.Plugin;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A class that loads plugin classes
 */
public class PluginLoader {

    /**
     * Default constructor
     */
    public PluginLoader() {
        // Do nothing
    }

    private final List<PluginClassLoader> pluginClassLoaders = new ArrayList<>();
    private final Map<String, Class<?>> pluginClasses = new HashMap<>();
    private final Map<String, List<String>> pluginDependencies = new HashMap<>();

    /**
     * Loads the plugins from the
     */
    public void loadPlugins() {
        File pluginDir = new File("plugins");
        if (pluginDir.exists() && pluginDir.isDirectory()) {
            File[] jarFiles = pluginDir.listFiles((_, name) -> name.endsWith(".jar"));
            if (jarFiles != null) {
                try {
                    URL[] urls = new URL[jarFiles.length];
                    for (int i = 0; i < jarFiles.length; i++) {
                        urls[i] = jarFiles[i].toURI().toURL();
                    }

                    PluginClassLoader pluginClassLoader = new PluginClassLoader(urls, getClass().getClassLoader());
                    pluginClassLoaders.add(pluginClassLoader);
                    // First Phase: Discover all plugin classes and their dependencies
                    for (File jarFile : jarFiles) {
                        discoverPlugins(jarFile, pluginClassLoader);
                    }

                    // Second Phase: Load all dependencies first
                    Set<String> loadedPlugins = new HashSet<>();
                    for (String pluginName : pluginClasses.keySet()) {
                        loadDependencies(pluginName, loadedPlugins, pluginClassLoader);
                    }
                } catch (Exception e) {
                    Logger.error("An error occurred whilst loading plugin files!", e);
                }
            }
        } else pluginDir.mkdir();
    }

    /**
     * Discover all plugins
     *
     * @param jarFile     The file to search
     * @param classLoader The loader to use
     */
    private void discoverPlugins(File jarFile, PluginClassLoader classLoader) {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Plugin.class)) {
                            Plugin annotation = clazz.getAnnotation(Plugin.class);
                            pluginClasses.put(annotation.name(), clazz);
                            pluginDependencies.put(annotation.name(), Arrays.asList(annotation.dependencies()));
                        }
                    } catch (Exception e) {
                        Logger.error(STR."An error occurred whilst loading plugin \{className}", e);
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("An error occurred whilst loading a plugin!", e);
        }
    }

    /**
     * Load dependencies first
     * @param pluginName the plugin name
     * @param loadedPlugins the loaded plugins
     * @param classLoader the class loader
     */
    private void loadDependencies(String pluginName, Set<String> loadedPlugins, PluginClassLoader classLoader) {
        if (loadedPlugins.contains(pluginName)) {
            return;
        }

        List<String> dependencies = pluginDependencies.get(pluginName);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                loadDependencies(dependency, loadedPlugins, classLoader);
            }
        }

        try {
            Class<?> pluginClass = pluginClasses.get(pluginName);
            if (pluginClass != null) {
                CytosisPlugin plugin = (CytosisPlugin) pluginClass.getDeclaredConstructor().newInstance();
                Logger.info(STR."Loading plugin: \{pluginName}");
                Cytosis.getPluginManager().registerPlugin(plugin, pluginClass.getAnnotation(Plugin.class));
                loadedPlugins.add(pluginName);
            }
        } catch (Exception e) {
            Logger.error(STR."Error loading plugin \{pluginName}!", e);
        }
    }
}
