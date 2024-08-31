package net.cytonic.cytosis.commands.debug;

import lombok.Getter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.objects.Preference;
import net.cytonic.objects.TypedNamespace;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.NamespaceID;

import java.util.UUID;

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
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.debug.preference"));
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<red>Please specify an operation!"));
        var opperationArg = ArgumentType.Enum("operation", Operation.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        opperationArg.setCallback((sender, _) -> sender.sendMessage(MM."<red>Invalid syntax!"));

        var nodeArg = ArgumentType.Word("node");
        nodeArg.setSuggestionCallback((_, _, suggestion) -> {
            for (NamespaceID preference : Cytosis.getPreferenceManager().getPreferenceRegistry().namespaces()) {
                suggestion.addEntry(new SuggestionEntry(preference.asString()));
            }
        });

        var valueArg = ArgumentType.StringArray("preference").setDefaultValue(new String[]{""});

        addSyntax(((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MM."<red>You must be a player to use this command!");
                return;
            }
            NamespaceID node = NamespaceID.from(context.get(nodeArg));
            PreferenceManager manager = Cytosis.getPreferenceManager();
            Preference<?> preference = manager.getPreferenceRegistry().get_UNSAFE(node);
            if (preference == null) {
                sender.sendMessage(MM."<red>Preference node <yellow>\{node.asString()}</yellow> does not exist!");
                return;
            }

            if (context.get(opperationArg) == Operation.SET) {
                TypedNamespace<?> typedNamespace = Cytosis.getPreferenceManager().getPreferenceRegistry().typedNamespaces().stream().filter(ns -> ns.namespaceID().equals(node)).findFirst().orElseThrow();
                Class<?> type = typedNamespace.type();
                String raw = context.get(valueArg)[0];
                Object value = null;
                if (raw.equalsIgnoreCase("null")) {
                    manager.updatePlayerPreference_UNSAFE(player.getUuid(), node, null);
                    player.sendMessage(MM."<#db0d74><b>NULLIFIED!</b></#db0d74> <gray>Successfully set preference node <yellow>\{node.asString()}</yellow> to null.");
                    return;
                }
                if (type.isEnum()) {
                    player.sendMessage(STR."<gray>Value: <yellow>\{raw}</yellow>");
                    try {
                        value = Enum.valueOf((Class<Enum>) type, raw);
                    } catch (Exception e) {
                        sender.sendMessage(MM."<red>The value <yellow>\{context.get(valueArg)[0]}</yellow> is not a valid enum value!");
                        return;
                    }
                } else if (type == String.class) {
                    value = String.join(" ", context.get(valueArg));
                } else if (type == Integer.class) {
                    value = Integer.parseInt(raw);
                } else if (type == Boolean.class) {
                    value = Boolean.parseBoolean(raw);
                } else if (type == UUID.class) {
                    value = UUID.fromString(raw);
                } else {
                    sender.sendMessage(MM."<red>The value <yellow>\{context.get(valueArg)[0]}</yellow> is not a valid value for preference node <yellow>\{node.asString()}</yellow>!");
                    return;
                }
                manager.updatePlayerPreference_UNSAFE(player.getUuid(), node, value);
                player.sendMessage(MM."<green>Successfully updated preference node <yellow>\{node.asString()}</yellow> to '<light_purple>\{value}</light_purple>'");
            } else if (context.get(opperationArg).equals(Operation.GET)) {
                Object pref = manager.getPlayerPreference_UNSAFE(player.getUuid(), node);
                sender.sendMessage(MM."<gray>The value of preference node <yellow>\{node.asString()}</yellow> is '<light_purple>\{pref}</light_purple>'");
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
