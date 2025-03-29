package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.arguments.ArgumentType;
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
        var typeArg = ArgumentType.Word("type").from("cytosis", "proxy");
        typeArg.setCallback((sender, e) -> sender.sendMessage(Msg.mm("<red>ERROR")));
        typeArg.setSuggestionCallback((cmds, cmdc, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("cytosis"));
            suggestion.addEntry(new SuggestionEntry("proxy"));
        });
        addSyntax((sender, context) -> {
            sender.sendMessage(Msg.whoops("This command is currently disabled."));
            String type = context.get(typeArg);
            if (type.equalsIgnoreCase("proxy")) {
//                Cytosis.getContainerizedInstanceManager().shutdownAllProxyInstances();
//                sender.sendMessage(Msg.mm("<GREEN>Shutting down Proxy instances!"));
            } else if (type.equalsIgnoreCase("cytosis")) {
//                Cytosis.getContainerizedInstanceManager().shutdownAllCytosisInstances();
//                sender.sendMessage(Msg.mm("<GREEN>Shutting down Cytosis instances!"));
            }
        }, typeArg);
    }
}
