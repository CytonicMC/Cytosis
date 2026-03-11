package net.cytonic.cytosis.commands.disabling;

import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
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
        ArgumentBoolean global = ArgumentType.Boolean("global");
        global.setDefaultValue(false);
        global.setSuggestionCallback((_, _, suggestion) -> {
            suggestion.addEntry(
                new SuggestionEntry("true", Msg.mm("<red>Enables this command on every server.</red>")));
            suggestion.addEntry(new SuggestionEntry("false",
                Msg.mm("<green>Enables this command only on <b>THIS</b> server.</green>")));
        });
        ArgumentStringArray cmd = ArgumentType.StringArray("cmd");
        cmd.setDefaultValue(new String[]{});

        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("Invalid Syntax! /enable <cmd>")));

        addSyntax((sender, context) -> {
            CommandDisablingManager commandDisablingManager = Cytosis.get(
                CommandDisablingManager.class);
            String rawCommand = String.join(" ", context.get(cmd));
            CytosisCommand command = commandDisablingManager.parseCommand(rawCommand);
            if (command == null) {
                sender.sendMessage(Msg.whoops("The command '%s' doesn't exist!", rawCommand));
                return;
            }

            if (context.get(global)) {
                if (!commandDisablingManager.isDisabledGlobally(rawCommand)) {
                    sender.sendMessage(Msg.whoops("This command is not globally disabled."));
                    return;
                }

                commandDisablingManager.enableCommandGlobally(rawCommand);
                commandDisablingManager.forAllSubcommands(
                    command,
                    subcommand -> commandDisablingManager.enableCommandGlobally(subcommand.getName()));
                sender.sendMessage(
                    Msg.greenSplash("ENABLED!", "enabled the '" + rawCommand + "' command on every server."));
                return;
            }

            if (!command.isDisabled()) {
                sender.sendMessage(Msg.whoops("This command is not disabled on this server."));
                return;
            }
            commandDisablingManager.enableCommandLocally(command);
            commandDisablingManager.forAllSubcommands(
                command,
                commandDisablingManager::enableCommandLocally);
            sender.sendMessage(
                Msg.greenSplash("ENABLED!", "enabled the '" + rawCommand + "' command on this server."));

        }, global, cmd);
    }
}