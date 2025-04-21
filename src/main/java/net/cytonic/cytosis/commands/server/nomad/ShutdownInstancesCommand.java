package net.cytonic.cytosis.commands.server.nomad;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.managers.ServerInstancingManager;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

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
        ArgumentWord typeArg = ArgumentType.Word("type").from(
                "cytosis", "cynder");
        typeArg.setSuggestionCallback((cmds, cmdc, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("cytosis"));
            suggestion.addEntry(new SuggestionEntry("cynder"));
        });

        setDefaultExecutor((sender, cmdc) -> sender.sendMessage(Msg.whoops("Usage: /shutdowninstances (type)")));

        addSyntax((sender, context) -> {
            String type = context.get(typeArg).toLowerCase();
            if (!ServerInstancingManager.isServerType(type)) {
                sender.sendMessage(Msg.whoops("Invalid instance type!"));
                return;
            }
            String niceName = Utils.captializeFirstLetters(type.replace("_", " "));
            Cytosis.getServerInstancingManager().deleteAllServerInstances(type);
            sender.sendMessage(Msg.success("Dispatched the deletion of all %s instances!", niceName));

        }, typeArg);
    }
}
