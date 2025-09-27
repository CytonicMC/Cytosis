package net.cytonic.cytosis.commands.chatting;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.containers.IgnoredChatChannelContainer;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;

public class IgnoreChatChannelCommand extends CytosisCommand {

    public IgnoreChatChannelCommand() {
        super("ignorechatchannel");
        ArgumentWord chatChannelArgument = ArgumentType.Word("channel")
            .from("mod", "admin", "staff", "all", "m", "ad", "s", "a");
        chatChannelArgument.setCallback(
            (sender, exception) -> sender.sendMessage("The channel " + exception.getInput() + " is invalid!"));
        chatChannelArgument.setSuggestionCallback((sender, commandContext, suggestion) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            addSuggestions(suggestion, player);
        });

        addSyntax(((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;

            ChatChannel channel = switch (context.get(chatChannelArgument).toLowerCase()) {
                case "all", "a" -> ChatChannel.ALL;
                case "admin", "ad" -> ChatChannel.ADMIN;
                case "mod", "m" -> ChatChannel.MOD;
                case "staff", "s" -> ChatChannel.STAFF;
                default -> throw new IllegalStateException(
                    "Unexpected value: " + context.get(chatChannelArgument).toLowerCase());
            };

            if (!player.canUseChannel(channel)) {
                player.sendMessage(Msg.whoops(
                    "You cannot ignore the " + channel.name().toLowerCase() + " because you don't have access to it!"));
                return;
            }
            ignoreChannel(player, channel);
        }), chatChannelArgument);
    }

    private void addSuggestions(Suggestion suggestion, CytosisPlayer player) {
        if (player.isModerator()) {
            suggestion.addEntry(new SuggestionEntry("mod"));
            suggestion.addEntry(new SuggestionEntry("m"));
        }
        if (player.isAdmin()) {
            suggestion.addEntry(new SuggestionEntry("admin"));
            suggestion.addEntry(new SuggestionEntry("ad"));
        }
        if (player.isStaff()) {
            suggestion.addEntry(new SuggestionEntry("staff"));
            suggestion.addEntry(new SuggestionEntry("s"));
        }
        suggestion.addEntry(new SuggestionEntry("all"));
        suggestion.addEntry(new SuggestionEntry("a"));
    }

    private void ignoreChannel(CytosisPlayer player, ChatChannel channel) {
        IgnoredChatChannelContainer container = player.getPreference(CytosisPreferences.IGNORED_CHAT_CHANNELS);
        container = container.withForChannel(channel, !container.getForChannel(channel));
        player.updatePreference(CytosisPreferences.IGNORED_CHAT_CHANNELS, container);

        if (!container.getForChannel(channel)) {
            player.sendMessage(Msg.greySplash("UNIGNORED!",
                "You are no longer ignoring the <gold>" + channel.name() + "</gold> chat."));
        } else {
            player.sendMessage(
                Msg.greySplash("IGNORED!", "You successfully muted <gold>" + channel.name() + "</gold> chat."));
        }
    }
}
