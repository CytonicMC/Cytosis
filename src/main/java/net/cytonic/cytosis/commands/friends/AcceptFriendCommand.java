package net.cytonic.cytosis.commands.friends;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.CytosisPreferences;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * Accepts a friend request
 */
public class AcceptFriendCommand extends Command {

    /**
     * A command to accept a friend
     */
    public AcceptFriendCommand() {
        super("acceptfriend", "faccept");

        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<red>Please specify a player to accept as a friend!"));
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

            if (!Cytosis.getCytonicNetwork().getLifetimePlayers().containsValue(context.get(playerArg))) {
                player.sendMessage(MM."<red>The player \{context.get(playerArg)} doesn't exist!");
                return;
            }

            UUID target = Cytosis.getCytonicNetwork().getLifetimePlayers().getByValue(context.get(playerArg));

            //todo: blocking system
            //todo: accept friend request
            //todo: handle errors
            Cytosis.getCynwaveWrapper().acceptFriendRequest(target, player.getUuid());

        }, playerArg);
    }
}