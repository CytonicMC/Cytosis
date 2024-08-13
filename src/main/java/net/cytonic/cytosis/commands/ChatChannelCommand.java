package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.enums.ChatChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

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
            if (sender.hasPermission("cytonic.chat.mod")) {
                suggestion.addEntry(new SuggestionEntry("mod"));
                suggestion.addEntry(new SuggestionEntry("m"));
            }
            if (sender.hasPermission("cytonic.chat.admin")) {
                suggestion.addEntry(new SuggestionEntry("admin"));
                suggestion.addEntry(new SuggestionEntry("ad"));
            }
            if (sender.hasPermission("cytonic.chat.staff")) {
                suggestion.addEntry(new SuggestionEntry("staff"));
                suggestion.addEntry(new SuggestionEntry("s"));
            }
            suggestion.addEntry(new SuggestionEntry("all"));
            suggestion.addEntry(new SuggestionEntry("a"));
        });

        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                ChatChannel chatChannel = switch (context.get(chatChannelArgument).toLowerCase()) {
                    case "all", "a" -> ChatChannel.ALL;
                    case "admin", "ad" -> ChatChannel.ADMIN;
                    case "mod", "m" -> ChatChannel.MOD;
                    case "staff", "s" -> ChatChannel.STAFF;
                    default -> throw new IllegalStateException(STR."Unexpected value: \{context.get(chatChannelArgument).toLowerCase()}");
                };
                if (!chatChannel.equals(Cytosis.getChatManager().getChannel(player.getUuid()))) {
                    message(player, chatChannel);
                } else player.sendMessage(MM."<RED>You are already in this channel!");
            } else {
                sender.sendMessage(Component.text("Hey! You can't do this.", NamedTextColor.RED));
            }
        }, chatChannelArgument);
    }

    private void message(Player player, ChatChannel channel) {
        if (channel == ChatChannel.ADMIN || channel == ChatChannel.MOD || channel == ChatChannel.STAFF) {
            if (player.hasPermission(channel.name().toLowerCase())) {
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