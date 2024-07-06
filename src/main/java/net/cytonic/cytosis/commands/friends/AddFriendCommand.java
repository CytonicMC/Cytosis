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
 * Sends a request to add a friend
 */
public class AddFriendCommand extends Command {

    /**
     * A command to add a friend
     */
    public AddFriendCommand() {
        super("addfriend", "fadd", "af");

        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<red>Please specify a player to add as a friend!"));
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

            if (!Cytosis.getCytonicNetwork().getOnlinePlayers().containsValue(context.get(playerArg))) {
                player.sendMessage(MM."<red>The player \{context.get(playerArg)} is not online!");
                return;
            }

            UUID target = Cytosis.getCytonicNetwork().getOnlinePlayers().getByValue(context.get(playerArg));
            if (!Cytosis.getPreferenceManager().getPlayerPreference(target, CytosisPreferences.ACCEPT_FRIEND_REQUESTS)) {
                player.sendMessage(MM."<red>The player \{context.get(playerArg)} does not accept friend requests!");
                return;
            }
            //todo: blocking system
            //todo: send friend request
            //todo: handle errors
            Cytosis.getCynwaveWrapper().sendFriendRequest(target, player.getUuid());

        }, playerArg);
    }
}
