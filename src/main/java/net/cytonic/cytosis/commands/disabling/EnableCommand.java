package net.cytonic.cytosis.commands.disabling;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.CommandDisablingManager;
import net.cytonic.cytosis.utils.Msg;

public class EnableCommand extends CytosisCommand {

    public EnableCommand() {
        super("enablecommand", "enable");
        setCondition(CommandUtils.IS_ADMIN);
        ArgumentWord cmd = ArgumentType.Word("cmd");
        ArgumentBoolean global = ArgumentType.Boolean("global");
        global.setDefaultValue(false);
        global.setSuggestionCallback((s, c, suggestion) -> {
            suggestion.addEntry(
                new SuggestionEntry("true", Msg.mm("<red>Enables this command on every server.</red>")));
            suggestion.addEntry(new SuggestionEntry("false",
                Msg.mm("<green>Enables this command only on <b>THIS</b> server.</green>")));
        });

        setDefaultExecutor((sender, context) -> sender.sendMessage(Msg.whoops("Invalid Syntax! /enable <cmd>")));

        addSyntax((sender, context) -> {
            String rawCommand = context.get(cmd);
            CommandDisablingManager commandDisablingManager = Cytosis.CONTEXT.getComponent(
                CommandDisablingManager.class);
            CommandManager commandManager = Cytosis.CONTEXT.getComponent(CommandManager.class);

            if (commandManager.getCommand(rawCommand) instanceof CytosisCommand command) {
                if (context.get(global)) {
                    if (!commandDisablingManager.isDisabledGlobally(command)) {
                        sender.sendMessage(Msg.whoops("This command is not globally disabled."));
                        return;
                    }
                    commandDisablingManager.enableCommandGlobally(command);
                    sender.sendMessage(
                        Msg.greenSplash("ENABLED!", "enabled the '" + rawCommand + "' command on every server."));
                    return;
                }

                if (!command.isDisabled()) {
                    sender.sendMessage(Msg.whoops("This command is not disabled on this server."));
                    return;
                }
                commandDisablingManager.enableCommandLocally(command);
                sender.sendMessage(
                    Msg.greenSplash("ENABLED!", "enabled the '" + rawCommand + "' command on this server."));
            }

        }, cmd, global);
    }
}