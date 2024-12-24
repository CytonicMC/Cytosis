package net.cytonic.cytosis.commands.chatting;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.enums.ChatChannel;
import net.cytonic.enums.PlayerRank;
import net.cytonic.objects.ChatMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.List;
import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

public class MsgCommand extends Command {

    public MsgCommand() {
        super("msg", "message", "whisper");

        var msgArgument = ArgumentType.StringArray("msg").setDefaultValue(new String[]{});
        msgArgument.setDefaultValue(new String[]{});
        var playerArg = ArgumentType.Word("player");
        playerArg.setSuggestionCallback((_, _, suggestion) -> Cytosis.getCytonicNetwork().getOnlinePlayers().getValues().forEach(player -> suggestion.addEntry(new SuggestionEntry(player))));
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>Usage: /msg <player> [message]"));

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                final String player = context.get(playerArg);
                final String msg = String.join(" ", context.get(msgArgument));
                if (!Cytosis.getCytonicNetwork().getOnlineFlattened().containsValue(player.toLowerCase())) {
                    sender.sendMessage(MM."<red>The player \{context.get(playerArg)} doesn't exist or is not online!");
                    return;
                }

                if (player.equalsIgnoreCase(actor.getUsername())) {
                    sender.sendMessage(MM."<red>You cannot message yourself!");
                    return;
                }

                UUID uuid = Cytosis.getCytonicNetwork().getOnlineFlattened().getByValue(player.toLowerCase());
                PlayerRank targetRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(uuid);

                if (msg.isEmpty()) {
                    Component recipient = targetRank.getPrefix().append(Component.text(Cytosis.getCytonicNetwork().getOnlinePlayers().getByKey(uuid), targetRank.getTeamColor()));
                    actor.sendMessage(MM."<b><dark_aqua>CHAT CHANNEL!</dark_aqua></b><gray> You opened a direct message to "
                            .append(recipient).append(MM."<gray>! <i><dark_gray>Whenever you type in chat your messages get sent to them!"));
                    Cytosis.getChatManager().setChannel(actor.getUuid(), ChatChannel.PRIVATE_MESSAGE);
                    Cytosis.getChatManager().openPrivateMessage(actor, uuid);
                    return;
                }


                Component component = MM."<dark_aqua>From <reset>".append(actor.getRank().getPrefix().append(MM."\{actor.getUsername()}")).append(MM."<dark_aqua> » ").append(Component.text(msg, NamedTextColor.WHITE));
                Cytosis.getDatabaseManager().getMysqlDatabase().addPlayerMessage(actor.getUuid(), uuid, msg);
                Cytosis.getNatsManager().sendChatMessage(new ChatMessage(List.of(uuid), ChatChannel.PRIVATE_MESSAGE, JSONComponentSerializer.json().serialize(component), actor.getUuid()));
                actor.sendMessage(MM."<dark_aqua>To <reset>".append(targetRank.getPrefix().append(MM."\{player}")).append(MM."<dark_aqua> » ").append(Component.text(msg, NamedTextColor.WHITE)));
            }
        }, playerArg, msgArgument);
    }
}
