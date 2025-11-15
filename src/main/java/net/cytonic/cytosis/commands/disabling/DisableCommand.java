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

public class DisableCommand extends CytosisCommand {

    public DisableCommand() {
        super("disablecommand", "disable");
        setCondition(CommandUtils.IS_ADMIN);
        ArgumentWord cmd = ArgumentType.Word("cmd");
        ArgumentBoolean global = ArgumentType.Boolean("global");
        global.setDefaultValue(false);
        global.setSuggestionCallback((s, c, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("true", Msg.red(
                "Disables this command on every server.<newline><dark_gray><i>Administrators are immune.")));
            suggestion.addEntry(new SuggestionEntry("false", Msg.green(
                "Disables this command only on <b>THIS</b> server.<newline><dark_gray><i>Administrators are immune.")));
        });

        setDefaultExecutor((sender, context) -> sender.sendMessage(Msg.whoops("Invalid Syntax! /disable <cmd>")));

        addSyntax((sender, context) -> {
            CommandDisablingManager commandDisablingManager = Cytosis.CONTEXT.getComponent(
                CommandDisablingManager.class);
            CommandManager commandManager = Cytosis.CONTEXT.getComponent(CommandManager.class);
            String rawCommand = context.get(cmd);

            if (commandManager.getCommand(rawCommand) instanceof CytosisCommand command) {
                if (context.get(global)) {
                    if (commandDisablingManager.isDisabledGlobally(command)) {
                        sender.sendMessage(Msg.whoops("This command is already globally disabled."));
                        return;
                    }
                    commandDisablingManager.disableCommandGlobally(command);
                    sender.sendMessage(
                        Msg.redSplash("DISABLED!", "disabled the '" + rawCommand + "' command on every server."));
                    return;
                }

                if (command.isDisabled()) {
                    sender.sendMessage(Msg.whoops("This command is already disabled on this server."));
                    return;
                }
                commandDisablingManager.disableCommandLocally(command);
                sender.sendMessage(
                    Msg.redSplash("DISABLED!", "disabled the '" + rawCommand + "' command on this server."));
            }

        }, cmd, global);
    }
}