package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * The class representing the time command
 */
public class TimeCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public TimeCommand() {
        super("time");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.time"));
        var timeArgument = ArgumentType.Word("time").from("day", "night", "noon", "sunset");
        timeArgument.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender.hasPermission("cytonic.commands.time")) {
                suggestion.addEntry(new SuggestionEntry("day"));
                suggestion.addEntry(new SuggestionEntry("night"));
                suggestion.addEntry(new SuggestionEntry("noon"));
                suggestion.addEntry(new SuggestionEntry("sunset"));
            }
        });
        setDefaultExecutor((sender, _) -> {
            if (sender.hasPermission("cytosis.commands.time")) {
                sender.sendMessage(MM."<RED>Usage: /time (time)");
            }
        });
        addSyntax((sender, context) -> {
            if (sender.hasPermission("cytosis.commands.time")) {
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
                };
            }
        }, timeArgument);
    }
}
