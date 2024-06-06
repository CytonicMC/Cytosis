package net.cytonic.cytosis.plugins;

import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.plugins.loader.PluginLoader;

import java.util.ArrayList;
import java.util.List;

public class PluginManager {
    private final List<CytosisPlugin> plugins = new ArrayList<>();
    private final PluginLoader pluginLoader = new PluginLoader();

    public void registerPlugin(CytosisPlugin plugin, Plugin annotation) {
        Logger.info(STR."Enabling plugin: \{annotation.name()}");
        plugins.add(plugin);
        plugin.initialize();
    }

    public void loadPlugins() {
        try {
            pluginLoader.loadPlugins();
        } catch (Exception e) {
            Logger.error("An error occurred whilst loading plugins!", e);
        }
        Logger.info(STR."Loaded \{plugins.size()} plugin(s)!");
    }
}
