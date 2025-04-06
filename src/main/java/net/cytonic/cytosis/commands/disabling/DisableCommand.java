package net.cytonic.cytosis.commands.disabling;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.managers.CommandDisablingManager;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;


public class DisableCommand extends CytosisCommand {

    public DisableCommand() {
        super("disablecommand", "disable");
        setCondition(CommandUtils.IS_ADMIN);
        var cmd = ArgumentType.Word("cmd");
        var global = ArgumentType.Boolean("global").setDefaultValue(false);
        global.setSuggestionCallback((s, c, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("true", Msg.mm("<red>Disables this command on every server.</red><newline><dark_gray><i>Administrators are immune.")));
            suggestion.addEntry(new SuggestionEntry("false", Msg.mm("<green>Disables this command only on <b>THIS</b> server.</green><newline><dark_gray><i>Administrators are immune.")));
        });

        setDefaultExecutor((sender, context) -> sender.sendMessage(Msg.whoops("Invalid Syntax! /disable <cmd>")));

        addSyntax((sender, context) -> {
            CommandDisablingManager manager = Cytosis.getCommandDisablingManager();
            String rawCommand = context.get(cmd);

            if (Cytosis.getCommandManager().getCommand(rawCommand) instanceof CytosisCommand command) {
                if (context.get(global)) {
                    if (manager.isDisabledGlobally(command)) {
                        sender.sendMessage(Msg.whoops("This command is already globally disabled."));
                        return;
                    }
                    manager.disableCommandGlobally(command);
                    sender.sendMessage(Msg.redSplash("DISABLED!", "disabled the '" + rawCommand + "' command on every server."));
                    return;
                }

                if (command.isDisabled()) {
                    sender.sendMessage(Msg.whoops("This command is already disabled on this server."));
                    return;
                }
                manager.disableCommandLocally(command);
                sender.sendMessage(Msg.redSplash("DISABLED!", "disabled the '" + rawCommand + "' command on this server."));
            }

        }, cmd, global);
    }
}
