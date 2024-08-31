package net.cytonic.cytosis.commands;

import com.google.gson.JsonObject;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.enums.ChatChannel;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import static net.cytonic.utils.MiniMessageTemplate.MM;

public class IgnoreChatChannelCommand extends Command {

    public IgnoreChatChannelCommand() {
        super("ignorechatchannel");
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

        addSyntax(((sender, context) -> {
            if (!(sender instanceof Player player)) {
                return;
            }
            ChatChannel channel = switch (context.get(chatChannelArgument).toLowerCase()) {
                case "all", "a" -> ChatChannel.ALL;
                case "admin", "ad" -> ChatChannel.ADMIN;
                case "mod", "m" -> ChatChannel.MOD;
                case "staff", "s" -> ChatChannel.STAFF;
                default -> throw new IllegalStateException(STR."Unexpected value: \{context.get(chatChannelArgument).toLowerCase()}");
            };
            JsonObject obj = Cytosis.GSON.fromJson(Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.IGNORED_CHAT_CHANNELS), JsonObject.class);
            if (obj.get(channel.name()).getAsBoolean()) {
                obj.addProperty(channel.name(), false);
                Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), CytosisNamespaces.IGNORED_CHAT_CHANNELS, obj.toString());
                player.sendMessage(MM."<gray><b>UNIGNORED!</b> You are no longer ignoring the <gold>\{channel.name()}</gold> chat.");
            } else {
                obj.addProperty(channel.name(), true);
                Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), CytosisNamespaces.IGNORED_CHAT_CHANNELS, obj.toString());
                player.sendMessage(MM."<gray><b>IGNORED!</b> You successfully muted <gold>\{channel.name()}</gold> chat.");
            }
        }), chatChannelArgument);
    }
}
