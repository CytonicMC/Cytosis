package net.cytonic.cytosis.commands.debug.preferences;

import java.util.UUID;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class SetPreferenceCommand extends CytosisCommand {

    public SetPreferenceCommand() {
        super("set");
        PreferenceManager pm = Cytosis.get(PreferenceManager.class);
        ArgumentWord nodeArg = ArgumentType.Word("node");
        nodeArg.setSuggestionCallback((cmds, cmdc, suggestion) -> {
            for (Key preference : pm.getPreferenceRegistry().namespaces()) {
                suggestion.addEntry(new SuggestionEntry(preference.asString()));
            }
        });

        ArgumentStringArray valueArg = (ArgumentStringArray) ArgumentType.StringArray("preference")
            .setDefaultValue(new String[]{""});

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                sender.sendMessage(Msg.red("You must be a player to use this command!"));
                return;
            }

            Key node = Key.key(context.get(nodeArg));
            PreferenceManager manager = pm;
            Preference<?> preference = manager.getPreferenceRegistry().unsafeGet(node);
            if (preference == null) {
                sender.sendMessage(Msg.red("Preference node <yellow>%s</yellow> does not exist!", node.asString()));
                return;
            }

            TypedNamespace<?> typedNamespace = pm.getPreferenceRegistry()
                .typedNamespaces().stream()
                .filter(ns -> ns.namespaceID().equals(node)).findFirst()
                .orElseThrow();
            Class<?> type = typedNamespace.type();
            String raw = context.get(valueArg)[0];
            if (raw.equalsIgnoreCase("null")) {
                manager.updateplayerpreferenceUnsafe(player.getUuid(), node, null);
                player.sendMessage(Msg.splash("NULLIFIED!", "db0d74",
                    "Successfully set preference node <yellow>%s</yellow> to null.", node.asString()));
                return;
            }
            Object value = parseValue(sender, type, raw, context.get(valueArg));

            manager.updateplayerpreferenceUnsafe(player.getUuid(), node, value);
            player.sendMessage(Msg.mm(
                "<green>Successfully updated preference node <yellow>%s</yellow> to <light_purple>'%s'</light_purple>",
                node.asString(), value));
        }, nodeArg, valueArg);
    }

    private Object parseValue(net.minestom.server.command.CommandSender sender, Class<?> type,
        String rawValue, String[] allValues) {
        try {
            if (type.isEnum()) {
                return parseEnumValue(sender, type, rawValue);
            } else if (type == String.class) {
                return String.join(" ", allValues);
            } else if (type == Integer.class) {
                return Integer.parseInt(rawValue);
            } else if (type == Boolean.class) {
                return Boolean.parseBoolean(rawValue);
            } else if (type == UUID.class) {
                return UUID.fromString(rawValue);
            } else {
                sender.sendMessage(Msg.mm(
                    "<red>Unsupported preference type: <yellow>%s</yellow>", type.getSimpleName()));
                return null;
            }
        } catch (Exception e) {
            sender.sendMessage(Msg.mm(
                "<red>Failed to parse value <yellow>%s</yellow> as %s: %s",
                rawValue, type.getSimpleName(), e.getMessage()));
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Object parseEnumValue(net.minestom.server.command.CommandSender sender,
        Class<?> type, String rawValue) {
        try {
            return Enum.valueOf((Class<Enum>) type, rawValue);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Msg.mm(
                "<red>The value <yellow>%s</yellow> is not a valid enum value for type %s!",
                rawValue, type.getSimpleName()));
            return null;
        }
    }

}
