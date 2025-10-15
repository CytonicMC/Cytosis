package net.cytonic.cytosis.commands.chatting;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

/**
 * The class representing the achat command
 */
public class AllChatCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public AllChatCommand() {
        super("achat", "ac");
        var chatMessage = ArgumentType.StringArray("chatMessage");
        setDefaultExecutor((sender, commandContext) -> {
            if (sender instanceof final Player player) {
                player.sendMessage(Msg.red("Usage: /achat (message)"));
            } else {
                sender.sendMessage(Msg.red("Only players may execute this command!"));
            }
        });
        addSyntax((sender, context) -> {
            if (sender instanceof final CytosisPlayer player) {
                final String message = String.join(" ", context.get(chatMessage));

                Component nonSelf = Component.text("").append(player.formattedName())
                    .append(Component.text(":", player.getRank().getChatColor())).appendSpace()
                    .append(Component.text(message, player.getRank().getChatColor()));

                Cytosis.getOnlinePlayers().forEach((p) -> {
                    if (!p.getUuid().equals(player.getUuid())) {
                        p.sendMessage(nonSelf);
                        return;
                    }

                    player.sendMessage(Component.text("").append(player.trueFormattedName())
                        .append(Component.text(":", player.getTrueRank().getChatColor())).appendSpace()
                        .append(Component.text(message, player.getTrueRank().getChatColor())));
                });
            } else {
                sender.sendMessage(Msg.red("Only players may execute this command!"));
            }
        }, chatMessage);
    }
}
