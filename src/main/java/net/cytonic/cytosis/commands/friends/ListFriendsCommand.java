package net.cytonic.cytosis.commands.friends;

import net.minestom.server.command.builder.Command;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * Sends a request to add a friend
 */
public class ListFriendsCommand extends Command {

    /**
     * A command to add a friend
     */
    public ListFriendsCommand() {
        super("friendlist", "flist", "fl");

        setDefaultExecutor((sender, _) -> {
            //TODO: implement
            sender.sendMessage(MM."<red>Implement me!");
        });

        //todo pagination


    }
}
