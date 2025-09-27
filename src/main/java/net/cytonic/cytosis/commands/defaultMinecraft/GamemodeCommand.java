package net.cytonic.cytosis.commands.defaultMinecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

/**
 * The class representing the gamemode command
 */
public class GamemodeCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public GamemodeCommand() {
        super("gamemode", "gm");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, cmdc) -> sender.sendMessage(Msg.red("You must specify a gamemode!")));

        // using a gamemode as an argument
        ArgumentEnum<@NotNull GameMode> gameModeArgument = ArgumentType.Enum("gamemode", GameMode.class)
            .setFormat(ArgumentEnum.Format.LOWER_CASED);
        gameModeArgument.setCallback(
            (sender, exception) -> sender.sendMessage("The gamemode " + exception.getInput() + " is invalid!"));

        ArgumentWord shorthand = ArgumentType.Word("shorthand").from("c", "s", "sv", "a", "0", "1", "2", "3");
        shorthand.setCallback((sender, exception) -> sender.sendMessage(
            Component.text("The shorthand '" + exception.getInput() + "' is invalid!", NamedTextColor.RED)));

        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                final GameMode gameMode = context.get(gameModeArgument);
                player.setGameMode(gameMode);
                player.sendMessage(Msg.success("Updated your gamemode to %s.", gameMode.name()));
            }
        }, gameModeArgument);

        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                final String gameMode = context.get(shorthand);
                GameMode gm = switch (gameMode) {
                    case "c", "1" -> GameMode.CREATIVE;
                    case "s", "3" -> GameMode.SPECTATOR;
                    case "a", "2" -> GameMode.ADVENTURE;
                    case "sv", "0" -> GameMode.SURVIVAL;
                    default -> {
                        sender.sendMessage(Msg.red("The shorthand '%s' is invalid!", gameMode));
                        throw new IllegalStateException();
                    }
                };

                player.setGameMode(gm);
                player.sendMessage(Component.text("Updated your gamemode to " + gm.name(), NamedTextColor.GREEN));
            }
        }, shorthand);
    }
}