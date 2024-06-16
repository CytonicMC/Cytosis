package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.ChatChannel;
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

        var chatChannelArgument = ArgumentType.Word("channel").from("mod", "admin", "staff", "all", "party", "league", "private_message");
        chatChannelArgument.setCallback((sender, exception) -> sender.sendMessage(STR."The channel \{exception.getInput()} is invalid!"));
        chatChannelArgument.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender.hasPermission("cytonic.chat.mod"))
                suggestion.addEntry(new SuggestionEntry("mod"));
            if (sender.hasPermission("cytonic.chat.admin"))
                suggestion.addEntry(new SuggestionEntry("admin"));
            if (sender.hasPermission("cytonic.chat.staff"))
                suggestion.addEntry(new SuggestionEntry("staff"));
            suggestion.addEntry(new SuggestionEntry("all"));
            suggestion.addEntry(new SuggestionEntry("party"));
            suggestion.addEntry(new SuggestionEntry("league"));
            suggestion.addEntry(new SuggestionEntry("private_message"));
        });

        var shorthand = ArgumentType.Word("shorthand").from("m", "ad", "s", "a");
        shorthand.setCallback((sender, exception) -> sender.sendMessage(STR."The shorthand \{exception.getInput()} is invalid!"));
        shorthand.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender.hasPermission("cytonic.chat.mod"))
                suggestion.addEntry(new SuggestionEntry("m"));
            if (sender.hasPermission("cytonic.chat.admin"))
                suggestion.addEntry(new SuggestionEntry("ad"));
            if (sender.hasPermission("cytonic.chat.staff"))
                suggestion.addEntry(new SuggestionEntry("s"));
            suggestion.addEntry(new SuggestionEntry("a"));
        });
        shorthand.setCallback((sender, exception) -> sender.sendMessage(Component.text(STR."The shorthand '\{exception.getInput()}' is invalid!", NamedTextColor.RED)));

        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                final String channel = context.get(chatChannelArgument);
                switch (channel) {
                    case "all" -> {
                        Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ALL);
                        player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("ALL", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
                        player.getAllPermissions().forEach((permission -> player.sendMessage(permission.getPermissionName())));
                    }
                    case "admin" -> {
                        if (player.hasPermission("cytonic.chat.admin")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ADMIN);
                            player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("ADMIN", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case "mod" -> {
                        if (player.hasPermission("cytonic.chat.mod")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.MOD);
                            player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("MOD", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case "staff" -> {
                        if (player.hasPermission("cytonic.chat.staff")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.STAFF);
                            player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("STAFF", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case "party" -> player.sendMessage("party");
                    case "league" -> player.sendMessage("league");
                    case "private message" -> player.sendMessage("private message");
                }
            } else {
                sender.sendMessage(Component.text("Hey! You can't do this.", NamedTextColor.RED));
            }
        }, chatChannelArgument);

        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                final String channel = context.get(shorthand);
                switch (channel.toLowerCase()) {
                    case "a" -> {
                        Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ALL);
                        player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("ALL", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
                    }
                    case "ad" -> {
                        if (player.hasPermission("cytonic.chat.admin")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ADMIN);
                            player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("ADMIN", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case "m" -> {
                        if (player.hasPermission("cytonic.chat.mod")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.MOD);
                            player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("MOD", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case "s" -> {
                        if (player.hasPermission("cytonic.chat.staff")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.STAFF);
                            player.sendMessage(Component.text("You are now in the ", NamedTextColor.GREEN).append(Component.text("STAFF", NamedTextColor.GOLD)).append(Component.text(" channel.", NamedTextColor.GREEN)));
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case "p" -> player.sendMessage("party");
                    case "l" -> player.sendMessage("league");
                    default ->
                            player.sendMessage(Component.text(STR."The shorthand '\{channel}' is invalid!", NamedTextColor.RED));
                }
            } else {
                sender.sendMessage(Component.text("Hey! You can't do this.", NamedTextColor.RED));
            }
        }, shorthand);
    }
}