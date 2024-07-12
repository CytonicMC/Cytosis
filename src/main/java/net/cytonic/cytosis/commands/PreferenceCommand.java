package net.cytonic.cytosis.commands;

import lombok.Getter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.NamespaceID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A command for updating the preferences of a player
 */
public class PreferenceCommand extends Command {

    /**
     * A simple command for debugging preferences
     */
    public PreferenceCommand() {
        super("preference", "pref");

        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<red>Please specify an operation!"));
        var opperationArg = ArgumentType.Enum("operation", Operation.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        opperationArg.setCallback((sender, _) -> sender.sendMessage(MM."<red>Invalid syntax!"));

        var nodeArg = ArgumentType.Word("node");
        nodeArg.setSuggestionCallback((_, _, suggestion) -> {
            for (NamespaceID preference : Cytosis.getPreferenceManager().getPreferenceRegistry().keySet()) {
                suggestion.addEntry(new SuggestionEntry(preference.asString()));
            }
        });

        var valueArg = ArgumentType.StringArray("value").setDefaultValue(new String[]{""});

        addSyntax(((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MM."<red>You must be a player to use this command!");
                return;
            }
            NamespaceID node = NamespaceID.from(context.get(nodeArg));
            PreferenceManager manager = Cytosis.getPreferenceManager();
            Class<?> type = manager.getPreferenceRegistry().get(node).value().getClass();
            if (context.get(opperationArg) == Operation.SET) {
                Object value = context.get(valueArg);
                if (type.isAssignableFrom(Integer.class)) {
                    value = Integer.parseInt(context.get(valueArg)[0]);
                } else if (type.isAssignableFrom(Boolean.class)) {
                    value = Boolean.parseBoolean(context.get(valueArg)[0]);
                } else if (type.isAssignableFrom(String.class)) {
                    value = String.join(" ", context.get(valueArg));
                }
                manager.updatePlayerPreference(player.getUuid(), node, value);
                player.sendMessage(MM."<green>Successfully updated preference node <yellow>\{node.asString()}</yellow> to '<light_purple>\{value}</light_purple>'");
            } else if (context.get(opperationArg).equals(Operation.GET)) {
                sender.sendMessage(MM."<gray>The value of preference node <yellow>\{node.asString()}</yellow> is '<light_purple>\{manager.getPlayerPreference(player.getUuid(), node)}</light_purple>'");
            }
        }), opperationArg, nodeArg, valueArg);
    }


    @Getter
    private enum Operation {
        SET("set"),
        GET("get");

        private final String name;

        Operation(String name) {
            this.name = name;
        }
    }
}
