package net.cytonic.cytosis.commands.chatting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

/**
 * A class that handles the chat channel command
 */
public class ChatChannelCommand extends CytosisCommand {

    /**
     * Creates the command
     */
    public ChatChannelCommand() {
        super("chat");
        setDefaultExecutor((sender, _) -> sender.sendMessage(
            Component.text("You must specify a channel!", NamedTextColor.RED)));

        ArgumentWord arg = ArgumentType.Word("channel")
            .from("mod", "admin", "staff", "party", "all", "m", "ad", "s", "p", "a");
        arg.setCallback((s, e) -> s.sendMessage(Msg.whoops("The channel %s is invalid!", e.getInput())));
        arg.setSuggestionCallback((sender, _, suggestion) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            if (player.isModerator()) suggestion.addEntry(new SuggestionEntry("mod"));
            if (player.isAdmin()) suggestion.addEntry(new SuggestionEntry("admin"));
            if (player.isStaff()) suggestion.addEntry(new SuggestionEntry("staff"));
            if (player.isInParty()) suggestion.addEntry(new SuggestionEntry("party"));
            suggestion.addEntry(new SuggestionEntry("all"));
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof final CytosisPlayer player)) return;

            ChatChannel channel = switch (context.get(arg).toLowerCase()) {
                case "all", "a" -> ChatChannel.ALL;
                case "admin", "ad" -> ChatChannel.ADMIN;
                case "mod", "m" -> ChatChannel.MOD;
                case "staff", "s" -> ChatChannel.STAFF;
                case "party", "p" -> ChatChannel.PARTY;
                default -> throw new IllegalStateException("Unexpected value: " + context.get(arg)
                    .toLowerCase());
            };
            if (!channel.equals(player.getChatChannel())) {
                message(player, channel);
                return;
            }

            player.sendMessage(Msg.whoops("You are already in the <gold>%s<gold> channel!", channel.name()));
        }, arg);
    }

    private void message(CytosisPlayer player, ChatChannel channel) {
        if (!player.canSendToChannel(channel)) {
            player.sendMessage(
                Msg.whoops("You do not have access to the <gold>%s</gold> channel right now.", channel.name()));
            return;
        }
        Cytosis.get(ChatManager.class).setChannel(player.getUuid(), channel);
        player.sendMessage(Msg.mm("<green>You are now in the <gold>%s</gold> channel.", channel.name()));
    }
}
