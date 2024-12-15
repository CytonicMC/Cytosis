package net.cytonic.cytosis.messaging.pubsub;

import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.enums.ChatChannel;
import net.cytonic.objects.ChatMessage;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minestom.server.sound.SoundEvent;
import redis.clients.jedis.JedisPubSub;

/**
 * A pub sub that handles chat channels
 */
@NoArgsConstructor
public class ChatMessages extends JedisPubSub {

    /**
     * Handles chat channel messages
     *
     * @param channel the channel that the message came from
     * @param message the message that was sent
     */
    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.CHAT_MESSAGES_CHANNEL)) return;
        ChatMessage chatMessage = ChatMessage.fromJson(message);
        ChatChannel chatChannel = chatMessage.channel();
        if (chatChannel == ChatChannel.ADMIN || chatChannel == ChatChannel.MOD || chatChannel == ChatChannel.STAFF) {
            Cytosis.getOnlinePlayers().forEach(player -> {
                if (player.canUseChannel(chatChannel) && !Cytosis.GSON.fromJson(Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.IGNORED_CHAT_CHANNELS), JsonObject.class).get(chatChannel.name()).getAsBoolean()) {
                    player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, .7f, 1.0F));
                    player.sendMessage(JSONComponentSerializer.json().deserialize(chatMessage.serializedMessage()));
                }
            });
        }
    }
}
