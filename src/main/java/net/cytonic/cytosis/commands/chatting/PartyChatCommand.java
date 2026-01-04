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

public class PartyChatCommand extends CytosisCommand {

    private final ChatManager cm = Cytosis.get(ChatManager.class);

    public PartyChatCommand() {
        super("partychat", "pchat", "pc");
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof final Player player)) return;
            player.sendMessage(Msg.whoops("Usage: /partychat [message]"));
        });
        ArgumentStringArray arg = ArgumentType.StringArray("message");
        addSyntax((sender, context) -> {
            if (!(sender instanceof final CytosisPlayer player)) return;
            String message = String.join(" ", context.get(arg));
            cm.sendMessage(message, ChatChannel.PARTY, player);
        }, arg);
    }
}
