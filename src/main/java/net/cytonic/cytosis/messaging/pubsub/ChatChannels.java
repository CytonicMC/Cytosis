package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.enums.ChatChannel;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minestom.server.sound.SoundEvent;
import redis.clients.jedis.JedisPubSub;

/**
 * A pub sub that handles chat channels
 */
public class ChatChannels extends JedisPubSub {

    /**
     * The default constructor
     */
    public ChatChannels() {
        // do nothing
    }

    /**
     * Handles chat channel messages
     *
     * @param channel the channel that the message came from
     * @param message the message that was sent
     */
    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.CHAT_CHANNELS_CHANNEL)) return;
        String[] thing = message.split("\\|:\\|");
        Component chatMessage = JSONComponentSerializer.json().deserialize(thing[0]);
        ChatChannel chatChannel = ChatChannel.valueOf(thing[1]);
        switch (chatChannel) {
            case MOD -> // send a message to all players with cytonic.chat.mod permission
                    Cytosis.getOnlinePlayers().forEach(player -> {
                        if (player.hasPermission("cytonic.chat.mod")) {
                            player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, .7f, 1.0F));
                            player.sendMessage(chatMessage);
                        }
                    });

            case STAFF -> // send a message to all players with cytonic.chat.staff permission
                    Cytosis.getOnlinePlayers().forEach(player -> {
                        if (player.hasPermission("cytonic.chat.staff")) {
                            player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, .7f, 1.0F));
                            player.sendMessage(chatMessage);
                        }
                    });
            case ADMIN -> // send a message to all players with cytonic.chat.admin permission
                    Cytosis.getOnlinePlayers().forEach(player -> {
                        if (player.hasPermission("cytonic.chat.admin")) {
                            player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, .7f, 1.0F));
                            player.sendMessage(chatMessage);
                        }
                    });
            case LEAGUE -> {// leagues..
            }
            case PARTY -> {// parties..
            }
        }
    }
}
