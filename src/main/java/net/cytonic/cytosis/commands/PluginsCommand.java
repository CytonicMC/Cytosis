package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.plugins.CytosisPlugin;
import net.cytonic.cytosis.plugins.Plugin;
import net.minestom.server.command.builder.Command;

import java.util.ArrayList;
import java.util.List;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * The class representing the plugins command
 */
public class PluginsCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public PluginsCommand() {
        super("plugins", "pl");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.plugins"));
        setDefaultExecutor((sender, _) -> {
            if (sender.hasPermission("cytosis.commands.plugins")) {
                List<Plugin> pluginsList = new ArrayList<>();
                StringBuilder result = new StringBuilder();
                for (CytosisPlugin plugin : Cytosis.getPluginManager().getPlugins()) {
                    Plugin annotation = plugin.getClass().getAnnotation(Plugin.class);

                    pluginsList.add(annotation);
                }

                for (int i = 0; i < pluginsList.size(); i++) {
                    Plugin plugin = pluginsList.get(i);

                    // Append the plugin name
                    result.append(plugin.name());

                    // Append seperation between plugin names.
                    if (i < pluginsList.size() - 1) {
                        result.append(", ");
                    }
                }

                sender.sendMessage(MM."<st><dark_green>                                                                                 ");
                if (pluginsList.size() > 0) sender.sendMessage(MM."<aqua><bold>PLUGINS:</bold></aqua> <green>\{result}");
                if (pluginsList.size() == 0) sender.sendMessage(MM."<aqua><bold>PLUGINS:</bold></aqua> <red>No plugins!");
                sender.sendMessage(MM."<st><dark_green>                                                                                 ");
            }
        });
    }
}
