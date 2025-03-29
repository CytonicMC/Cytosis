package net.cytonic.cytosis.commands.chatting;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

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
                player.sendMessage(Msg.mm("<RED>Usage: /achat (message)"));
            } else {
                sender.sendMessage(Msg.mm("<RED>Only players may execute this command!"));
            }
        });
        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                Component message = Component.text("")
                        .append(Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getPrefix())
                        .append(Component.text(player.getUsername(), (Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getTeamColor())))
                        .append(Component.text(":", Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor()))
                        .appendSpace()
                        .append(Component.text(String.join(" ", context.get(chatMessage)), Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor()));
                Cytosis.getOnlinePlayers().forEach((p) -> p.sendMessage(message));
            } else {
                sender.sendMessage(Msg.mm("<RED>Only players may execute this command!"));
            }
        }, chatMessage);
    }
}
