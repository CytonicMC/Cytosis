package net.cytonic.cytosis.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.kyori.adventure.text.Component;

public class ChatManager {

    private final Map<UUID, ChatChannel> channels = new HashMap<>();
    public void removeChannel(UUID uuid) {
        channels.remove(uuid);
    }
    public void setChannel(UUID uuid, ChatChannel channel) {
        channels.put(uuid, channel);
    }

    public ChatChannel getChannel(UUID uuid) {
        return channels.getOrDefault(uuid, ChatChannel.ALL);
    }

    public void sendMessageToChannel(Component component, ChatChannel chatChannel) {
        switch (chatChannel) {
            case ADMIN,MOD,STAFF -> // send a message to all servers
                    Cytosis.getMessagingManager().getRabbitMQ().sendChatMessage(component, chatChannel);
            case PARTY -> {
                // parties..
            }
            case LEAGUE -> {
                // leagues..
            }
            case PRIVATE_MESSAGE -> {
                // private messages
            }
            case ALL -> throw new UnsupportedOperationException("Unimplemented case: " + chatChannel);
            default -> throw new IllegalArgumentException("Unexpected value: " + chatChannel);
        }
    }
}