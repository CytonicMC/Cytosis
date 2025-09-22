package net.cytonic.cytosis.commands.friends;

import java.util.List;
import java.util.UUID;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;

/**
 * A command to handle friend actions
 */
public class FriendCommand extends CytosisCommand {

    static final ArgumentWord NON_FRIEND_ARG = (ArgumentWord) ArgumentType.Word("player")
        .setSuggestionCallback((sender, context, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {

                List<UUID> friends = Cytosis.getFriendManager().getFriends(player.getUuid());

                for (String networkPlayer : Cytosis.getCytonicNetwork().getOnlinePlayers().getValues()) {
                    if (networkPlayer.equalsIgnoreCase(player.getUsername())) {
                        continue;
                    }
                    if (friends.contains(Cytosis.getCytonicNetwork().getOnlinePlayers().getByValue(networkPlayer))) {
                        continue;
                    }
                    suggestion.addEntry(new SuggestionEntry(networkPlayer));
                }
            }
        });

    /**
     * Creates the command
     */
    public FriendCommand() {
        super("friend", "f");
        setCondition(Conditions::playerOnly);

        addSubcommand(new FriendAddCommand());
        addSubcommand(new FriendRemoveCommand());
        addSubcommand(new FriendAcceptCommand());
        addSubcommand(new FriendDeclineCommand());
        addSubcommand(new FriendListCommand());
    }
}
