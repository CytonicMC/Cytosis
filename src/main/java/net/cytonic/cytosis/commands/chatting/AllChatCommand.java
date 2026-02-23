package net.cytonic.cytosis.commands.chatting;

import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

/**
 * The class representing the allchat command
 */
public class AllChatCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public AllChatCommand() {
        super("allchat", "achat", "ac");
        ArgumentStringArray chatMessage = ArgumentType.StringArray("chatMessage");
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof final Player player)) return;
            player.sendMessage(Msg.red("Usage: /achat (message)"));
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof final CytosisPlayer player)) return;

            final String message = String.join(" ", context.get(chatMessage));

            Cytosis.get(ChatManager.class).sendMessage(message, ChatChannel.ALL, player);
        }, chatMessage);
    }
}
