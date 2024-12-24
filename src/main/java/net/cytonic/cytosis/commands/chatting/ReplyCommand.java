package net.cytonic.cytosis.commands.chatting;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;

import static net.cytonic.utils.MiniMessageTemplate.MM;

public class ReplyCommand extends Command {
    public ReplyCommand() {
        super("reply", "r");
        var msgArgument = ArgumentType.StringArray("msg");
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(MM."<red><b></b></red><gray> you must have a message to reply with!");
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                return;
            }

            final String msg = String.join(" ", context.get(msgArgument));

            if (!Cytosis.getChatManager().hasOpenPrivateChannel(player)) {
                player.sendMessage(MM."<red><b>WHOOPS!</b></red><gray> You don't have an open conversation!");
                return;
            }

            Cytosis.getChatManager().handlePrivateMessage(msg, player);
        }, msgArgument);
    }
}
