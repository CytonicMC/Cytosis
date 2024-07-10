package net.cytonic.cytosis.commands.friends;

import net.cytonic.cytosis.Cytosis;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * Sends a request to add a friend
 */
public class RemoveFriendCommand extends Command {

    /**
     * A command to add a friend
     */
    public RemoveFriendCommand() {
        super("removefriend", "frem");

        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<red>Please specify a player to remove from your friends list!"));
        var playerArg = ArgumentType.Word("player");
        playerArg.setSuggestionCallback((_, _, suggestion) -> {
            for (String networkPlayer : Cytosis.getCytonicNetwork().getOnlinePlayers().getValues()) {
                suggestion.addEntry(new SuggestionEntry(networkPlayer));
            }
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MM."<red>You must be a player to use this command!");
                return;
            }


            UUID target = Cytosis.getCytonicNetwork().getOnlinePlayers().getByValue(context.get(playerArg));

            if (target == player.getUuid()) {
                player.sendMessage(MM."<red>You cannot remove yourself as a friend!");
            }

        }, playerArg);
    }
}
