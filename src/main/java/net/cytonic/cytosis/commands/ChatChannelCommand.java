package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

public class ChatChannelCommand extends Command {

    public ChatChannelCommand() {
        super("chat");
        setDefaultExecutor(((sender, commandContext) -> sender.sendMessage(Component.text("You must specify a channel!", NamedTextColor.RED))));

         var chatChannelArgument = ArgumentType.Enum("channel", ChatChannel.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
         chatChannelArgument.setCallback((sender, exception) -> sender.sendMessage(STR."The channel \{exception.getInput()} is invalid!"));

         var shorthand = ArgumentType.Word("shorthand").from("a");
        shorthand.setSuggestionCallback((sender, commandContext, suggestion) -> {
            Player player = (Player) sender;
            if (player.hasPermission("cytonic.chat.mod"))
                suggestion.addEntry(new SuggestionEntry("m", Component.text("Represents the Mod channel")));
            if (player.hasPermission("cytonic.chat.staff"))
                suggestion.addEntry(new SuggestionEntry("s", Component.text("Represents the Staff channel")));
            if (player.hasPermission("cytonic.chat.admin"))
                suggestion.addEntry(new SuggestionEntry("ad", Component.text("Represents the Admin channel")));
                //parties
            suggestion.addEntry(new SuggestionEntry("a",Component.text("Represents the All channel")));
        });
        shorthand.setCallback((sender, exception) -> sender.sendMessage(Component.text(STR."The shorthand '\{exception.getInput()}' is invalid!", NamedTextColor.RED)));
        
        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                final ChatChannel channel = context.get(chatChannelArgument);
                switch (channel) {
                    case ALL -> {
                        Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ALL);
                        player.sendMessage(
                                Component.text("You are now in the ", NamedTextColor.GREEN)
                                        .append(Component.text("ALL", NamedTextColor.GOLD))
                                        .append(Component.text(" channel.", NamedTextColor.GREEN))
                        );
                    }
                    case ADMIN -> {
                        if (player.hasPermission("cytonic.chat.admin")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ADMIN);
                            player.sendMessage(
                                    Component.text("You are now in the ", NamedTextColor.GREEN)
                                            .append(Component.text("ADMIN", NamedTextColor.GOLD))
                                            .append(Component.text(" channel.", NamedTextColor.GREEN))
                            );
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case MOD -> {
                        if (player.hasPermission("cytonic.chat.mod")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.MOD);
                            player.sendMessage(
                                    Component.text("You are now in the ", NamedTextColor.GREEN)
                                            .append(Component.text("MOD", NamedTextColor.GOLD))
                                            .append(Component.text(" channel.", NamedTextColor.GREEN))
                            );
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case STAFF -> {
                        if (player.hasPermission("cytonic.chat.staff")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.STAFF);
                            player.sendMessage(
                                    Component.text("You are now in the ", NamedTextColor.GREEN)
                                            .append(Component.text("STAFF", NamedTextColor.GOLD))
                                            .append(Component.text(" channel.", NamedTextColor.GREEN))
                            );
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case PARTY -> {
                        player.sendMessage("party");
                    }
                    case LEAGUE -> {
                        player.sendMessage("league");
                    }
                    case PRIVATE_MESSAGE -> {
                        player.sendMessage("private message");
                    }
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
                        player.sendMessage(
                                Component.text("You are now in the ", NamedTextColor.GREEN)
                                        .append(Component.text("ALL", NamedTextColor.GOLD))
                                        .append(Component.text(" channel.", NamedTextColor.GREEN))
                        );
                    }
                    case "ad" -> {
                        if (player.hasPermission("cytonic.chat.admin")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ADMIN);
                            player.sendMessage(
                                    Component.text("You are now in the ", NamedTextColor.GREEN)
                                            .append(Component.text("ADMIN", NamedTextColor.GOLD))
                                            .append(Component.text(" channel.", NamedTextColor.GREEN))
                            );
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case "m" -> {
                        if (player.hasPermission("cytonic.chat.mod")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.MOD);
                            player.sendMessage(
                                    Component.text("You are now in the ", NamedTextColor.GREEN)
                                            .append(Component.text("MOD", NamedTextColor.GOLD))
                                            .append(Component.text(" channel.", NamedTextColor.GREEN))
                            );
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case "s" -> {
                        if (player.hasPermission("cytonic.chat.staff")) {
                            Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.STAFF);
                            player.sendMessage(
                                    Component.text("You are now in the ", NamedTextColor.GREEN)
                                            .append(Component.text("STAFF", NamedTextColor.GOLD))
                                            .append(Component.text(" channel.", NamedTextColor.GREEN))
                            );
                        } else {
                            player.sendMessage(Component.text("You do not have access to this channel.", NamedTextColor.RED));
                        }
                    }
                    case "p" -> {
                        player.sendMessage("party");
                    }
                    case "l" -> {
                        player.sendMessage("league");
                    }
                    default -> player.sendMessage(Component.text(STR."The shorthand '\{channel}' is invalid!", NamedTextColor.RED));
                }
            } else {
                sender.sendMessage(Component.text("Hey! You can't do this.", NamedTextColor.RED));
            }
        }, shorthand);
}
}