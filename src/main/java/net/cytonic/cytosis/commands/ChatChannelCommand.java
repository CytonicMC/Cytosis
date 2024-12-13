package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.enums.ChatChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A class that handles the chat channel command
 */
public class ChatChannelCommand extends Command {

    /**
     * Creates the command
     */
    public ChatChannelCommand() {
        super("chat");
        setDefaultExecutor((sender, _) -> sender.sendMessage(Component.text("You must specify a channel!", NamedTextColor.RED)));

        var chatChannelArgument = ArgumentType.Word("channel").from("mod", "admin", "staff", "all", "m", "ad", "s", "a");
        chatChannelArgument.setCallback((sender, exception) -> sender.sendMessage(STR."The channel \{exception.getInput()} is invalid!"));
        chatChannelArgument.setSuggestionCallback((sender, _, suggestion) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                return;
            }
            if (player.isModerator()) {
                suggestion.addEntry(new SuggestionEntry("mod"));
            }
            if (player.isAdmin()) {
                suggestion.addEntry(new SuggestionEntry("admin"));
            }
            if (player.isStaff()) {
                suggestion.addEntry(new SuggestionEntry("staff"));
            }
            suggestion.addEntry(new SuggestionEntry("all"));
        });

        addSyntax((sender, context) -> {
            if (sender instanceof final CytosisPlayer player) {
                ChatChannel chatChannel = switch (context.get(chatChannelArgument).toLowerCase()) {
                    case "all", "a" -> ChatChannel.ALL;
                    case "admin", "ad" -> ChatChannel.ADMIN;
                    case "mod", "m" -> ChatChannel.MOD;
                    case "staff", "s" -> ChatChannel.STAFF;
                    default ->
                            throw new IllegalStateException(STR."Unexpected value: \{context.get(chatChannelArgument).toLowerCase()}");
                };
                if (!chatChannel.equals(Cytosis.getChatManager().getChannel(player.getUuid()))) {
                    message(player, chatChannel);
                } else player.sendMessage(MM."<RED>You are already in this channel!");
            } else {
                sender.sendMessage(Component.text("Hey! You can't do this.", NamedTextColor.RED));
            }
        }, chatChannelArgument);
    }

    private void message(CytosisPlayer player, ChatChannel channel) {
        if (channel == ChatChannel.ADMIN || channel == ChatChannel.MOD || channel == ChatChannel.STAFF) {
            if (player.canUseChannel(channel)) {
                Cytosis.getChatManager().setChannel(player.getUuid(), channel);
                player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text(channel.name(), NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
            }
            return;
        }
        if (channel == ChatChannel.ALL) {
            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ALL);
            player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("ALL", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
        }
    }


}