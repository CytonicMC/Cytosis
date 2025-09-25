package net.cytonic.cytosis.commands.friends;

import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;

public class FriendRemoveCommand extends CytosisCommand {

    public FriendRemoveCommand() {
        super("remove");
        ArgumentWord playerArg = ArgumentType.Word("player");
        playerArg.setSuggestionCallback((sender, context, suggestion) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            for (UUID networkPlayer : Cytosis.CONTEXT.getComponent(FriendManager.class)
                .getFriends(player.getUuid())) {
                suggestion.addEntry(new SuggestionEntry(
                    Cytosis.CONTEXT.getComponent(CytonicNetwork.class).getLifetimePlayers()
                        .getByKey(networkPlayer)));
            }
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            UUID target = PlayerUtils.resolveUuid(context.get(playerArg));
            if (target == null) {
                player.sendMessage(Msg.whoops("The player '%s' doesn't exist!", context.get(playerArg)));
                return;
            }

            if (target.equals(player.getUuid())) {
                player.sendMessage(Msg.whoops("You cannot remove yourself from your friends list!"));
                return;
            }

            CytonicNetwork network = Cytosis.CONTEXT.getComponent(CytonicNetwork.class);
            String name = network.getLifetimePlayers().getByKey(target);
            Component targetComp = network.getCachedPlayerRanks().get(target).getPrefix()
                .append(Component.text(name));

            if (!Cytosis.CONTEXT.getComponent(FriendManager.class).getFriends(player.getUuid()).contains(target)) {
                player.sendMessage(
                    Msg.whoops("The player ").append(targetComp).append(Msg.grey(" is not on your friends list!")));
                return;
            }

            player.removeFriend(target);
        }, playerArg);
    }
}
