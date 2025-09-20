package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.instance.InstanceContainer;

/**
 * The class representing the time command
 */
public class TimeCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public TimeCommand() {
        super("time");
        setCondition(CommandUtils.IS_STAFF);
        var timeArgument = ArgumentType.Word("time").from("day", "night", "noon", "midnight", "sunrise", "sunset", "freeze");
        var timeInteger = ArgumentType.Integer("timeInteger");
        timeArgument.setSuggestionCallback((sender, cmdc, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("day"));
            suggestion.addEntry(new SuggestionEntry("night"));
            suggestion.addEntry(new SuggestionEntry("noon"));
            suggestion.addEntry(new SuggestionEntry("midnight"));
            suggestion.addEntry(new SuggestionEntry("sunrise"));
            suggestion.addEntry(new SuggestionEntry("sunset"));
            suggestion.addEntry(new SuggestionEntry("freeze"));
        });
        setDefaultExecutor((sender, cmdc) -> sender.sendMessage(Msg.mm("<RED>Usage: /time (time)")));
        InstanceContainer defaultInstance = Cytosis.CONTEXT.getComponent(InstanceContainer.class);
        addSyntax((sender, context) -> {
            switch (context.get(timeArgument).toLowerCase()) {
                case "day" -> {
                    defaultInstance.setTime(1000); // Day
                    sender.sendMessage(Msg.mm("<GREEN>Time set to day."));
                }
                case "night" -> {
                    defaultInstance.setTime(13000); // Night
                    sender.sendMessage(Msg.mm("<GREEN>Time set to night."));
                }
                case "midnight" -> {
                    defaultInstance.setTime(18000); // Midnight
                    sender.sendMessage(Msg.mm("<GREEN>Time set to midnight."));
                }
                case "noon" -> {
                    defaultInstance.setTime(6000); // Noon
                    sender.sendMessage(Msg.mm("<GREEN>Time set to noon."));
                }
                case "sunrise" -> {
                    defaultInstance.setTime(23000); // Sunrise
                    sender.sendMessage(Msg.mm("<GREEN>Time set to sunrise."));
                }
                case "sunset" -> {
                    defaultInstance.setTime(12000); // Sunset
                    sender.sendMessage(Msg.mm("<GREEN>Time set to sunset."));
                }
                case "freeze" -> {
                    if (defaultInstance.getTimeRate() == 1) {
                        defaultInstance.setTimeRate(0);
                        sender.sendMessage(Msg.mm("<green>Time frozen."));
                    } else if (defaultInstance.getTimeRate() == 0) {
                        defaultInstance.setTimeRate(1);
                        sender.sendMessage(Msg.mm("<green>Time unfrozen."));
                    }
                }
            }

        }, timeArgument);
        addSyntax((sender, context) -> {
            defaultInstance.setTime(context.get(timeInteger)); // Set time to input
            sender.sendMessage(Msg.mm("<GREEN>Time set to " + context.get(timeInteger) + "."));
        }, timeInteger);
    }
}