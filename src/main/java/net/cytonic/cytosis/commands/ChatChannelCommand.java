package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.utils.MiniMessageTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

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

        var chatChannelArgument = ArgumentType.Word("channel").from("mod", "admin", "staff", "all", "party", "league", "private_message","m","ad","s","a","p","l");
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
            suggestion.addEntry(new SuggestionEntry("party"));
            suggestion.addEntry(new SuggestionEntry("p"));
            suggestion.addEntry(new SuggestionEntry("league"));
            suggestion.addEntry(new SuggestionEntry("l"));
            suggestion.addEntry(new SuggestionEntry("private_message"));
        });

        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                ChatChannel chatChannel = switch (context.get(chatChannelArgument).toLowerCase()) {
                    case "all", "a" -> ChatChannel.ALL;
                    case "admin", "ad" -> ChatChannel.ADMIN;
                    case "mod", "m" -> ChatChannel.MOD;
                    case "staff", "s" -> ChatChannel.STAFF;
                    case "party", "p" -> ChatChannel.PARTY;
                    case "league", "l" -> ChatChannel.LEAGUE;
                    case "private_message" -> ChatChannel.PRIVATE_MESSAGE;
                    default -> throw new IllegalStateException(STR."Unexpected value: \{context.get(chatChannelArgument).toLowerCase()}");
                };
                if (!chatChannel.name().equals(Cytosis.getChatManager().getChannel(player.getUuid()).name())) {
                    message(player, chatChannel);
                } else player.sendMessage(MiniMessageTemplate.MM."<RED>You are already in this channel!");
            } else {
                sender.sendMessage(Component.text("Hey! You can't do this.", NamedTextColor.RED));
            }
        }, chatChannelArgument);
    }

    private void message(Player player, ChatChannel channel) {
        switch (channel) {
            case ALL -> {
                Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ALL);
                player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("ALL", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
            }
            case ADMIN -> {
                if (player.hasPermission("cytonic.chat.admin")) {
                    Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ADMIN);
                    player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("ADMIN", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
                } else {
                    player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                }
            }
            case MOD -> {
                if (player.hasPermission("cytonic.chat.mod")) {
                    Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.MOD);
                    player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("MOD", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
                } else {
                    player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                }
            }
            case STAFF -> {
                if (player.hasPermission("cytonic.chat.staff")) {
                    Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.STAFF);
                    player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("STAFF", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
                } else {
                    player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                }
            }
            case PARTY -> player.sendMessage("party");
            case LEAGUE -> player.sendMessage("league");
            case PRIVATE_MESSAGE -> player.sendMessage("private message");
        }
    }
}