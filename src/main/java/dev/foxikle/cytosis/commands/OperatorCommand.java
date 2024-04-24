package dev.foxikle.cytosis.commands;

import dev.foxikle.cytosis.Cytosis;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandParser;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.List;

public class OperatorCommand extends Command {

    public OperatorCommand() {
        super("op");
        setCondition((sender, commandString) -> sender.hasPermission("cytosis.commands.operator"));

        var playerArg = ArgumentType.Word("player").from(getPlayerNames());
        playerArg.setCallback((sender, exception) -> {
            final String input = exception.getInput();
            sender.sendMessage("The player " + input + " is invalid!");
        });

        setDefaultExecutor((sender, context) -> {
            if (sender == Cytosis.getConsoleSender()) {
                CommandParser.Result result = Cytosis.getCommandManager().parseCommand(sender, context.getInput());
                System.out.println(result.args());
                return;
            }
            final String playerName = context.get(playerArg);
            Cytosis.getPlayer(playerName).ifPresentOrElse(Cytosis::opPlayer, () -> sender.sendMessage(Component.text("Couldn't find the player '" + playerName + "'. Did you spell their name right?")));
        });
    }

    private String[] getPlayerNames() {
        List<Player> players = Cytosis.getOnlinePlayers().stream().toList();
        String[] names = new String[players.size()];
        for (int i = 0; i < players.size(); i++) {
            names[i] = players.get(i).getUsername();
        }
        return names;
    }

}
