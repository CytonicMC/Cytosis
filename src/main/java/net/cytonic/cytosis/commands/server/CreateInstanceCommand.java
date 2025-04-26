package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.commands.util.CommandUtils;
import net.cytonic.cytosis.commands.util.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

/**
 * A command to create a new instance
 */
public class CreateInstanceCommand extends CytosisCommand {

    /**
     * A command to create a new instance
     */
    public CreateInstanceCommand() {
        super("createinstance");
        setCondition(CommandUtils.IS_ADMIN);
        var typeArg = ArgumentType.Word("type").from("cytosis", "proxy");
        typeArg.setCallback((sender, e) -> sender.sendMessage(Msg.mm("<red>ERROR")));
        typeArg.setSuggestionCallback((cmds, cmdc, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("cytosis"));
            suggestion.addEntry(new SuggestionEntry("proxy"));
        });
        addSyntax((sender, context) -> {
            String type = context.get(typeArg);
            sender.sendMessage(Msg.whoops("This command is currently disabled."));
            switch (type.toLowerCase()) {
                case "proxy" -> {
//                    Cytosis.getContainerizedInstanceManager().createProxyInstance();
//                    sender.sendMessage(Msg.mm("<GREEN>Created new Proxy instance! It may take a few seconds to fully start up."));
                }
                case "cytosis" -> {
//                    Cytosis.getContainerizedInstanceManager().createCytosisInstance();
//                    sender.sendMessage(Msg.mm("<GREEN>Created new Cytosis instance! It may take a few seconds to fully start up."));
                }
            }
        });
    }
}
