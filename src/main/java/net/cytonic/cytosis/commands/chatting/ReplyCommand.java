package net.cytonic.cytosis.commands.chatting;

import net.minestom.server.command.builder.arguments.ArgumentType;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class ReplyCommand extends CytosisCommand {

    public ReplyCommand() {
        super("reply", "r");
        var msgArgument = ArgumentType.StringArray("msg");
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(Msg.whoops("You must have a message to reply with!"));
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;

            final String msg = String.join(" ", context.get(msgArgument));

            ChatManager chatManager = Cytosis.get(ChatManager.class);
            if (!chatManager.hasOpenPrivateChannel(player)) {
                player.sendMessage(Msg.whoops("You don't have an open conversation!"));
                return;
            }

            chatManager.handlePrivateMessage(msg, player);
        }, msgArgument);
    }
}