package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * The class representing the time command
 */
public class TimeCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public TimeCommand() {
        super("time");
        setCondition(CommandUtils.IS_STAFF);
        var timeArgument = ArgumentType.Word("time").from("day", "night", "noon", "midnight", "sunrise", "sunset");
        var timeInteger = ArgumentType.Integer("timeInteger");
        timeArgument.setSuggestionCallback((sender, _, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("day"));
            suggestion.addEntry(new SuggestionEntry("night"));
            suggestion.addEntry(new SuggestionEntry("noon"));
            suggestion.addEntry(new SuggestionEntry("midnight"));
            suggestion.addEntry(new SuggestionEntry("sunrise"));
            suggestion.addEntry(new SuggestionEntry("sunset"));
        });
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>Usage: /time (time)"));
        addSyntax((sender, context) -> {
            switch (context.get(timeArgument).toLowerCase()) {
                case "day" -> {
                    Cytosis.getDefaultInstance().setTime(1000); // Day
                    sender.sendMessage(MM."<GREEN>Time set to day.");
                }
                case "night" -> {
                    Cytosis.getDefaultInstance().setTime(13000); // Night
                    sender.sendMessage(MM."<GREEN>Time set to night.");
                }
                case "midnight" -> {
                    Cytosis.getDefaultInstance().setTime(18000); // Midnight
                    sender.sendMessage(MM."<GREEN>Time set to midnight.");
                }
                case "noon" -> {
                    Cytosis.getDefaultInstance().setTime(6000); // Noon
                    sender.sendMessage(MM."<GREEN>Time set to noon.");
                }
                case "sunrise" -> {
                    Cytosis.getDefaultInstance().setTime(23000); // Sunrise
                    sender.sendMessage(MM."<GREEN>Time set to sunrise.");
                }
                case "sunset" -> {
                    Cytosis.getDefaultInstance().setTime(12000); // Sunset
                    sender.sendMessage(MM."<GREEN>Time set to sunset.");
                }
            }

        }, timeArgument);
        addSyntax((sender, context) -> {
            Cytosis.getDefaultInstance().setTime(context.get(timeInteger)); // Set time to input
            sender.sendMessage(MM."<GREEN>Time set to \{context.get(timeInteger)}.");
        }, timeInteger);
    }
}
