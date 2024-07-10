package net.cytonic.cytosis.commands.friends;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend", "f");
        setCondition(Conditions::playerOnly);

        addSubcommand(new AddFriendCommand());
        addSubcommand(new AcceptFriendCommand());
        addSubcommand(new DeclineFriendCommand());
        addSubcommand(new ListFriendsCommand());
        addSubcommand(new RemoveFriendCommand());
    }
}
