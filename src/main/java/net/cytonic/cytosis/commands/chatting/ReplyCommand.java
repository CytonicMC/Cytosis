package net.cytonic.cytosis.commands.chatting;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.arguments.ArgumentType;

public class ReplyCommand extends CytosisCommand {
    public ReplyCommand() {
        super("reply", "r");
        var msgArgument = ArgumentType.StringArray("msg");
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(Msg.whoops("You must have a message to reply with!"));
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                return;
            }

            final String msg = String.join(" ", context.get(msgArgument));

            if (!Cytosis.getChatManager().hasOpenPrivateChannel(player)) {
                player.sendMessage(Msg.whoops("You don't have an open conversation!"));
                return;
            }

            Cytosis.getChatManager().handlePrivateMessage(msg, player);
        }, msgArgument);
    }
}
