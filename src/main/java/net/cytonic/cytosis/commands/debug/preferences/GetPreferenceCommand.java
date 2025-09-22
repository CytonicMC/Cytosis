package net.cytonic.cytosis.commands.debug.preferences;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class GetPreferenceCommand extends CytosisCommand {

    public GetPreferenceCommand() {
        super("get");

        ArgumentWord nodeArg = ArgumentType.Word("node");
        nodeArg.setSuggestionCallback((cmds, cmdc, suggestion) -> {
            for (Key preference : Cytosis.getPreferenceManager().getPreferenceRegistry().namespaces()) {
                suggestion.addEntry(new SuggestionEntry(preference.asString()));
            }
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                sender.sendMessage(Msg.mm("<red>You must be a player to use this command!"));
                return;
            }
            Key node = Key.key(context.get(nodeArg));
            PreferenceManager manager = Cytosis.getPreferenceManager();
            Preference<?> preference = manager.getPreferenceRegistry().unsafeGet(node);
            if (preference == null) {
                player.sendMessage(
                    Msg.mm("<red>Preference node <yellow>" + node.asString() + "</yellow> does not exist!"));
            }
        }, nodeArg);
    }
}
