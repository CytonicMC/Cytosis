package net.cytonic.cytosis.commands.chatting;

import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.notifyPackets.ChatMessageNotifyPacket;

public class MsgCommand extends CytosisCommand {

    public MsgCommand() {
        super("msg", "message", "whisper");

        var msgArgument = ArgumentType.StringArray("msg").setDefaultValue(new String[]{});
        msgArgument.setDefaultValue(new String[]{});
        var playerArg = ArgumentType.Word("player");
        CytonicNetwork network = Cytosis.get(CytonicNetwork.class);
        playerArg.setSuggestionCallback((_, _, suggestion) -> network.getOnlinePlayers().getValues()
            .forEach(player -> suggestion.addEntry(new SuggestionEntry(player))));
        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.mm("<RED>Usage: /msg <player> [message]")));

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                final String player = context.get(playerArg);
                final String msg = String.join(" ", context.get(msgArgument));
                if (!network.getOnlineFlattened().containsValue(player.toLowerCase())) {
                    sender.sendMessage(Msg.mm(
                        "<red>The player " + context.get(playerArg) + " doesn't exist or is " + "not" + " online!"));
                    return;
                }

                if (player.equalsIgnoreCase(actor.getUsername())) {
                    sender.sendMessage(Msg.mm("<red>You cannot message yourself!"));
                    return;
                }
                UUID recipient = network.getOnlineFlattened().getByValue(player.toLowerCase());

                sendMessage(actor, recipient, msg, network);
            }
        }, playerArg, msgArgument);
    }

    private void sendMessage(CytosisPlayer actor, UUID recipient, String message, CytonicNetwork network) {
        PlayerRank targetRank = network.getCachedPlayerRanks().get(recipient);

        if (message.isEmpty()) {
            Component recipientComponent = targetRank.getPrefix()
                .append(Component.text(network.getOnlinePlayers().getByKey(recipient), targetRank.getTeamColor()));
            actor.sendMessage(
                Msg.darkAquaSplash("CHAT CHANNEL!", "You opened a direct message to ").append(recipientComponent)
                    .append(Msg.grey("!"))
                    .append(Msg.darkGrey("<i>Whenever you type in chat your messages get sent to" + " them!")));
            ChatManager chatManager = Cytosis.get(ChatManager.class);
            chatManager.setChannel(actor.getUuid(), ChatChannel.PRIVATE_MESSAGE);
            chatManager.openPrivateMessage(actor, recipient);
            return;
        }

        Component component = Msg.mm("<dark_aqua>From <reset>")
            .append(actor.getRank().getPrefix().append(Msg.mm(actor.getUsername())).append(Msg.mm("<dark_aqua> » ")))
            .append(Component.text(message, NamedTextColor.WHITE));
        Cytosis.get(MysqlDatabase.class).addPlayerMessage(actor.getUuid(), recipient, message);
        new ChatMessageNotifyPacket.Packet(Set.of(recipient), ChatChannel.PRIVATE_MESSAGE, new JsonComponent(component),
            actor.getUuid()).publish();
        actor.sendMessage(Msg.mm("<dark_aqua>To <reset>").append(targetRank.getPrefix().append(
                Msg.mm(Cytosis.get(CytonicNetwork.class).getLifetimeFlattened().getByKey(recipient))))
            .append(Msg.mm("<dark_aqua> » ")).append(Component.text(message, NamedTextColor.WHITE)));
    }
}