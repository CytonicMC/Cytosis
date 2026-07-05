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
import net.cytonic.cytosis.player.OfflinePlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;
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
            UUID uuid = PlayerUtils.resolveNickedUuid(player);
            if (uuid != null) { // this player is nicked and doesn't accept message requests
                actor.whoops("%s doesn't accept private messages.", Players.miniNameFragile(player));
                return;
            }
            uuid = PlayerUtils.resolveUuid(player);
            OfflinePlayer p;
            try {
                p = Players.offline(uuid);
            } catch (NullPointerException e) {
                actor.whoops("The player %s doesn't exist!", context.get(playerArg));
                return;
            }

            if (!network.isOnline(p.uuid())) {
                actor.whoops("%s is not online!", Players.trueMiniName(p.uuid()));
                return;
            }

            if (player.equalsIgnoreCase(actor.getUsername())) {
                actor.whoops("You cannot message yourself!");
                return;
            }

            sendMessage(actor, p.uuid(), msg);

        }, playerArg, msgArgument);
    }

    private void sendMessage(CytosisPlayer actor, UUID recipient, String message) {
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