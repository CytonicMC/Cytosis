package net.cytonic.cytosis.commands.chatting;

import java.util.UUID;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Players;

public class MsgCommand extends CytosisCommand {

    public MsgCommand() {
        super("msg", "message", "whisper");

        var msgArgument = ArgumentType.StringArray("msg").setDefaultValue(new String[]{});
        msgArgument.setDefaultValue(new String[]{});
        var playerArg = ArgumentType.Word("player");
        CytonicNetwork network = Cytosis.get(CytonicNetwork.class);
        playerArg.setSuggestionCallback((_, _, suggestion) -> network.getOnlinePlayers().getValues()
            .forEach(player -> suggestion.addEntry(new SuggestionEntry(player))));
        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.red("Usage: /msg <player> [message]")));

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer actor)) return;

            final String player = context.get(playerArg);
            final String msg = String.join(" ", context.get(msgArgument));
            if (!network.getOnlineFlattened().containsValue(player.toLowerCase())) {
                actor.whoops("The player %s doesn't exist or is not online!", context.get(playerArg));
                return;
            }

            if (player.equalsIgnoreCase(actor.getUsername())) {
                actor.whoops("You cannot message yourself!");
                return;
            }
            UUID recipient = network.getOnlineFlattened().getByValue(player.toLowerCase());

            sendMessage(actor, recipient, msg, network);

        }, playerArg, msgArgument);
    }

    private void sendMessage(CytosisPlayer actor, UUID recipient, String message, CytonicNetwork network) {
        if (message.isEmpty()) {
            actor.sendMessage(Msg.darkAquaSplash("CHAT CHANNEL!",
                "You opened a direct message to %s! <dark_gray><i>Whenever you type in chat your messages get sent to them!",
                Players.trueMiniName(recipient)));
            ChatManager chatManager = Cytosis.get(ChatManager.class);
            chatManager.setChannel(actor.getUuid(), ChatChannel.PRIVATE_MESSAGE);
            chatManager.openPrivateMessage(actor, recipient);
            return;
        }

        Cytosis.get(ChatManager.class).handlePrivateMessage(message, actor, recipient);
    }
}