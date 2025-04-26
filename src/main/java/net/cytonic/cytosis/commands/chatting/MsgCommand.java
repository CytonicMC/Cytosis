package net.cytonic.cytosis.commands.chatting;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.util.CytosisCommand;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.ChatMessage;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.List;
import java.util.UUID;

public class MsgCommand extends CytosisCommand {

    public MsgCommand() {
        super("msg", "message", "whisper");

        var msgArgument = ArgumentType.StringArray("msg").setDefaultValue(new String[]{});
        msgArgument.setDefaultValue(new String[]{});
        var playerArg = ArgumentType.Word("player");
        playerArg.setSuggestionCallback((cmds, cmdc, suggestion) -> Cytosis.getCytonicNetwork().getOnlinePlayers().getValues().forEach(player -> suggestion.addEntry(new SuggestionEntry(player))));
        setDefaultExecutor((sender, cmdc) -> sender.sendMessage(Msg.mm("<RED>Usage: /msg <player> [message]")));

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                final String player = context.get(playerArg);
                final String msg = String.join(" ", context.get(msgArgument));
                if (!Cytosis.getCytonicNetwork().getOnlineFlattened().containsValue(player.toLowerCase())) {
                    sender.sendMessage(Msg.mm("<red>The player " + context.get(playerArg) + " doesn't exist or is not online!"));
                    return;
                }

                if (player.equalsIgnoreCase(actor.getUsername())) {
                    sender.sendMessage(Msg.mm("<red>You cannot message yourself!"));
                    return;
                }

                UUID uuid = Cytosis.getCytonicNetwork().getOnlineFlattened().getByValue(player.toLowerCase());
                PlayerRank targetRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(uuid);

                if (msg.isEmpty()) {
                    Component recipient = targetRank.getPrefix().append(Component.text(Cytosis.getCytonicNetwork().getOnlinePlayers().getByKey(uuid), targetRank.getTeamColor()));
                    actor.sendMessage(Msg.darkAquaSplash("CHAT CHANNEL!", "You opened a direct message to ")
                            .append(recipient).append(Msg.mm("<gray>! <i><dark_gray>Whenever you type in chat your messages get sent to them!")));
                    Cytosis.getChatManager().setChannel(actor.getUuid(), ChatChannel.PRIVATE_MESSAGE);
                    Cytosis.getChatManager().openPrivateMessage(actor, uuid);
                    return;
                }


                Component component = Msg.mm("<dark_aqua>From <reset>").append(actor.getRank().getPrefix().append(Msg.mm(actor.getUsername())).append(Msg.mm("<dark_aqua> » "))).append(Component.text(msg, NamedTextColor.WHITE));
                Cytosis.getDatabaseManager().getMysqlDatabase().addPlayerMessage(actor.getUuid(), uuid, msg);
                Cytosis.getNatsManager().sendChatMessage(new ChatMessage(List.of(uuid), ChatChannel.PRIVATE_MESSAGE, JSONComponentSerializer.json().serialize(component), actor.getUuid()));
                actor.sendMessage(Msg.mm("<dark_aqua>To <reset>").append(targetRank.getPrefix().append(Msg.mm(player))).append(Msg.mm("<dark_aqua> » ")).append(Component.text(msg, NamedTextColor.WHITE)));
            }
        }, playerArg, msgArgument);
    }
}
