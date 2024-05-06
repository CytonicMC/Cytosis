package net.cytonic.cytosis.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GamemodeCommand extends Command {

    public GamemodeCommand() {
        super("gamemode", "gm");
        setCondition((sender, commandString) -> sender.hasPermission("cytosis.commands.gamemode"));
        setDefaultExecutor((sender, context) -> sender.sendMessage(Component.text("You must specify a gamemode!", NamedTextColor.RED)));
        // using a gamemode as an argument
        var gameModeArgument = ArgumentType.Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        gameModeArgument.setCallback((sender, exception) -> sender.sendMessage(STR."The gamemode \{exception.getInput()} is invalid!"));
        var shorthand = ArgumentType.Word("shorthand").from("c", "s", "sv", "a", "0", "1", "2", "3");
        shorthand.setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("c", Component.text("Represents the Creative gamemode")));
            suggestion.addEntry(new SuggestionEntry("s", Component.text("Represents the Spectator gamemode")));
            suggestion.addEntry(new SuggestionEntry("sv", Component.text("Represents the Survival gamemode")));
            suggestion.addEntry(new SuggestionEntry("a", Component.text("Represents the Adventure gamemode")));
        });
        shorthand.setCallback((sender, exception) -> sender.sendMessage(Component.text(STR."The shorthand '\{exception.getInput()}' is invalid!", NamedTextColor.RED)));
        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                final GameMode gameMode = context.get(gameModeArgument);
                player.setGameMode(gameMode);
                player.sendMessage(Component.text(STR."Updated your gamemode to \{gameMode.name()}", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("Hey! You can't do this.", NamedTextColor.RED));
            }
        }, gameModeArgument);

        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                final String gameMode = context.get(shorthand);
                GameMode gm = null;
                switch (gameMode) {
                    case "c", "1" -> gm = GameMode.CREATIVE;
                    case "s", "3" -> gm = GameMode.SPECTATOR;
                    case "a", "2" -> gm = GameMode.ADVENTURE;
                    case "sv", "0" -> gm = GameMode.SURVIVAL;
                }
                if (gm == null) {
                    sender.sendMessage(Component.text(STR."The shorthand '\{gameMode}' is invalid!", NamedTextColor.RED));
                    return;
                }
                player.setGameMode(gm);
                player.sendMessage(Component.text(STR."Updated your gamemode to \{gm.name()}", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("Hey! You can't do this.", NamedTextColor.RED));
            }
        }, shorthand);
    }
}