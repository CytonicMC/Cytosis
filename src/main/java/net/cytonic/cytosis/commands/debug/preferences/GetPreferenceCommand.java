package net.cytonic.cytosis.commands.debug.preferences;

import java.util.Set;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class GetPreferenceCommand extends CytosisCommand {

    public GetPreferenceCommand() {
        super("get");

        ArgumentWord nodeArg = ArgumentType.Word("node");
        nodeArg.setSuggestionCallback((cmds, cmdc, suggestion) -> {
            if (!(cmds instanceof CytosisPlayer player)) return;
            Set<Key> options = Cytosis.get(PreferenceManager.class).getPreferenceRegistry().keys();
            options.addAll(player.getPreferenceKeys());
            for (Key preference : options) {
                suggestion.addEntry(new SuggestionEntry(preference.asString()));
            }
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;

            Key node = Key.key(context.get(nodeArg));
            PreferenceManager manager = Cytosis.get(PreferenceManager.class);
            Object preference = manager.getPlayerPreference_UNSAFE(player.getUuid(), node);
            if (preference == null) {
                player.sendMessage(Msg.red("Preference node <yellow>" + node.asString() + "</yellow> does not exist!"));
                return;
            }
            player.sendMessage(
                Msg.pink("Preference node <yellow>" + node.asString() + "</yellow> has a value of '<aqua>%s</aqua>'",
                    preference.toString()));
        }, nodeArg);
    }
}
