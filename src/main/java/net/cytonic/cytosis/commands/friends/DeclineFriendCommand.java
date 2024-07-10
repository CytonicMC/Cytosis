package net.cytonic.cytosis.commands.friends;

import net.cytonic.cytosis.Cytosis;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.Objects;
import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * Sends a request to add a friend
 */
public class DeclineFriendCommand extends Command {

    /**
     * A command to add a friend
     */
    public DeclineFriendCommand() {
        super("declinefriend", "fdecline");

        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<red>Please specify a player to decline a friend request from!"));
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
                player.sendMessage(MM."<red>You cannot decline a friend request from yourself!");
                return;
            }

            Cytosis.getCynwaveWrapper().declineFriendRequest(target, player.getUuid()).whenComplete((s, throwable) -> {
                if (throwable != null) {
                    player.sendMessage(MM."<red>Error: " + throwable.getMessage());
                } else {
                    if (Objects.equals(s, "NOT_FOUND")) {
                        player.sendMessage(MM."<red>You don't have an active friend request from \{context.get(playerArg)}!!");
                    } else if (Objects.equals(s, "UNAUTHORIZED")) {
                        player.sendMessage(MM."<red>For some reason, you don't have permission to decline a friend request from \{context.get(playerArg)}!");
                    }
                }
            });

        }, playerArg);
    }
}
