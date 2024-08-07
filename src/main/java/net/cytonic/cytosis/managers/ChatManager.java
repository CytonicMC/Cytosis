package net.cytonic.cytosis.managers;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.CytosisNamespaces;
import net.cytonic.cytosis.data.enums.CytosisPreferences;
import net.cytonic.enums.ChatChannel;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * This class handles chat messages and channels
 */
@SuppressWarnings("unused")
public class ChatManager {

    /**
     * A default constructor for the ChatManager
     */
    public ChatManager() {
    }

    /**
     * Sets a specified players chat channel
     *
     * @param uuid    The player to set
     * @param channel The channel to set
     */
    public void setChannel(UUID uuid, ChatChannel channel) {
        Cytosis.getPreferenceManager().updatePlayerPreference(uuid, CytosisNamespaces.CHAT_CHANNEL, channel);
    }

    /**
     * Gets the player's chat channel
     *
     * @param uuid The player's uuid
     * @return the player's currently selected chat channel
     */
    public ChatChannel getChannel(UUID uuid) {
        return Cytosis.getPreferenceManager().getPlayerPreference(uuid, CytosisPreferences.CHAT_CHANNEL);
    }

    /**
     * Sends a message out to redis.
     * @param originalMessage The original content of the message
     * @param channel The channel to send the message to
     * @param player The player who sent the message
     */
    public void sendMessage(String originalMessage, ChatChannel channel, Player player) {
        if (!originalMessage.contains("|:|")) {
            Component channelComponent = Component.empty();
            if (channel != ChatChannel.ALL) {
                channelComponent = channel.getPrefix();
            }
            Component message = Component.text("")
                    .append(channelComponent)
                    .append(Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getPrefix())
                    .append(Component.text(player.getUsername(), (Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getTeamColor())))
                    .append(Component.text(":", Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor()))
                    .appendSpace()
                    .append(Component.text(originalMessage, Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor()));
            if (channel == ChatChannel.ALL) {
                Cytosis.getOnlinePlayers().forEach((p) -> p.sendMessage(message));
            } else {
                Cytosis.getDatabaseManager().getRedisDatabase().sendChatMessage(message, channel);
            }
        } else player.sendMessage(MM."<red>Hey you cannot do that!");
    }
}