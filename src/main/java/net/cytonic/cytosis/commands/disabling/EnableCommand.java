package net.cytonic.cytosis.commands.disabling;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.CommandDisablingManager;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;


public class EnableCommand extends CytosisCommand {

    public EnableCommand() {
        super("enablecommand", "enable");
        setCondition(CommandUtils.IS_ADMIN);
        var cmd = ArgumentType.Word("cmd");
        var global = ArgumentType.Boolean("global").setDefaultValue(false);
        global.setSuggestionCallback((s, c, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("true", Msg.mm("<red>Enables this command on every server.</red>")));
            suggestion.addEntry(new SuggestionEntry("false", Msg.mm("<green>Enables this command only on <b>THIS</b> server.</green>")));
        });

        setDefaultExecutor((sender, context) -> sender.sendMessage(Msg.whoops("Invalid Syntax! /enable <cmd>")));

        addSyntax((sender, context) -> {
            String rawCommand = context.get(cmd);
            CommandDisablingManager manager = Cytosis.getCommandDisablingManager();

            if (Cytosis.getCommandManager().getCommand(rawCommand) instanceof CytosisCommand command) {
                if (context.get(global)) {
                    if (!manager.isDisabledGlobally(command)) {
                        sender.sendMessage(Msg.whoops("This command is not globally disabled."));
                        return;
                    }
                    manager.enableCommandGlobally(command);
                    sender.sendMessage(Msg.greenSplash("ENABLED!", "enabled the '" + rawCommand + "' command on every server."));
                    return;
                }

                if (!command.isDisabled()) {
                    sender.sendMessage(Msg.whoops("This command is not disabled on this server."));
                    return;
                }
                manager.enableCommandLocally(command);
                sender.sendMessage(Msg.greenSplash("ENABLED!", "enabled the '" + rawCommand + "' command on this server."));
            }

        }, cmd, global);
    }
}
