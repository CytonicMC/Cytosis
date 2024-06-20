package net.cytonic.cytosis.managers;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    private final Map<UUID, ChatChannel> channels = new HashMap<>();

    /**
     * Removes the players from the channel list
     *
     * @param uuid The player to remove
     */
    public void removeChannel(UUID uuid) {
        channels.remove(uuid);
    }

    /**
     * Sets a specified players chat channel
     * @param uuid The player to set
     * @param channel The channel to set
     */
    public void setChannel(UUID uuid, ChatChannel channel) {
        channels.put(uuid, channel);
        Cytosis.getDatabaseManager().getMysqlDatabase().setChatChannel(uuid, channel);
    }

    /**
     * Gets the player's chat channel
     * @param uuid The player to
     * @return the player's curretly selected chat channel
     */
    public ChatChannel getChannel(UUID uuid) {
        return channels.getOrDefault(uuid, ChatChannel.ALL);
    }

    /**
     * Sends a message to the specified chat channel
     * @param component The message
     * @param chatChannel The channel to send the message to
     */
    public void sendMessageToChannel(Component component, ChatChannel chatChannel) {
        switch (chatChannel) {
            case ADMIN, MOD, STAFF -> // send a message to all servers
                    Cytosis.getMessagingManager().getRabbitMQ().sendChatMessage(component, chatChannel);
            case PARTY -> {
                //todo parties..
            }
            case LEAGUE -> {
                //todo leagues..
            }
            case PRIVATE_MESSAGE -> {
                //todo private messages
            }
            case ALL -> throw new UnsupportedOperationException(STR."Unimplemented case: \{chatChannel}");
            default -> throw new IllegalArgumentException(STR."Unexpected value: \{chatChannel}");
        }
    }

    public void getShorthandChatChannel(ChatChannel channel) {
        switch (channel) {}
    }
}