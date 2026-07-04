package net.cytonic.cytosis.commands.server.nomad;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.ServerInstancingManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

/**
 * A command for closing all instances of cytosis on a kubernetes cluster
 */
public class ShutdownInstancesCommand extends CytosisCommand {

    /**
     * A command to close all instances
     */
    public ShutdownInstancesCommand() {
        super("shutdowninstances");
        setCondition(CommandUtils.IS_ADMIN);
        ArgumentWord typeArg = ArgumentType.Word("type").from("cytosis", "cynder");
        typeArg.setSuggestionCallback((_, _, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("cytosis"));
            suggestion.addEntry(new SuggestionEntry("cynder"));
        });

        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("Usage: /shutdowninstances (type)")));

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String type = context.get(typeArg).toLowerCase();
            if (!ServerInstancingManager.isServerType(type)) {
                player.whoops("Invalid instance type!");
                return;
            }

            String niceName = Utils.captializeFirstLetters(type.replace("_", " "));
            Cytosis.get(ServerInstancingManager.class).deleteAllServerInstances(type);
            player.success("Dispatched the deletion of all %s instances!", niceName);
        }, typeArg);
    }
}