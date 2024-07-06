package net.cytonic.cytosis.plugins;

import lombok.Getter;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.plugins.loader.PluginLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that manages plugins
 */
public class PluginManager {

    /**
     * Default constructor
     */
    public PluginManager() {
        // Do nothing
    }

    @Getter private final List<CytosisPlugin> plugins = new ArrayList<>();
    private final PluginLoader pluginLoader = new PluginLoader();

    /**
     * Registers a plugin
     *
     * @param plugin     The plugin
     * @param annotation the plugin annotation
     */
    public void registerPlugin(CytosisPlugin plugin, Plugin annotation) {
        Logger.info(STR."Enabling plugin: \{annotation.name()}");
        plugins.add(plugin);
        plugin.initialize();
    }

    /**
     * Loads the plugins in the plugins folder
     */
    public void loadPlugins() {
        try {
            pluginLoader.loadPlugins();
        } catch (Exception e) {
            Logger.error("An error occurred whilst loading plugins!", e);
        }
        Logger.info(STR."Loaded \{plugins.size()} plugin(s)!");
    }
}
