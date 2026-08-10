package net.cytonic.cytosis.commands.friends;

import java.util.UUID;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.player.OfflinePlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Players;

@SubCommand
class RemoveCommand extends CytosisCommand {

    RemoveCommand() {
        super("remove");
        ArgumentWord playerArg = ArgumentType.Word("player");
        playerArg.setSuggestionCallback((sender, _, suggestion) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            for (UUID networkPlayer : player.getFriends()) {
                suggestion.addEntry(new SuggestionEntry(
                    Cytosis.get(CytonicNetwork.class).getLifetimePlayers()
                        .getByKey(networkPlayer)));
            }
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            UUID uuid = Players.resolveNickedUuid(context.get(playerArg));
            if (uuid != null) { // this player is nicked
                player.whoops("%s is not on your friends list!", Players.miniNameFragile(context.get(playerArg)));
                return;
            }

            UUID target = Players.resolveUuid(context.get(playerArg));
            OfflinePlayer targetObj;
            try {
                targetObj = Players.offline(target);
            } catch (NullPointerException e) {
                player.whoops("The player '%s' doesn't exist!", context.get(playerArg));
                return;
            }

            if (targetObj.uuid().equals(player.getUuid())) {
                player.whoops("You cannot remove yourself from your friends list!");
                return;
            }

            String name = Players.miniNameFragile(context.get(playerArg));

            if (!player.getFriends().contains(target)) {
                player.sendMessage(
                    Msg.whoops("%s is not on your friends list!", name));
                return;
            }

            player.removeFriend(target);
        }, playerArg);
    }
}
