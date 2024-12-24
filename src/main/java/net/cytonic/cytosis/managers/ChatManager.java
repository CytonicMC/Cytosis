package net.cytonic.cytosis.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.enums.ChatChannel;
import net.cytonic.enums.PlayerRank;
import net.cytonic.objects.ChatMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * This class handles chat messages and channels
 */
@SuppressWarnings("unused")
@NoArgsConstructor
public class ChatManager {

    private final Cache<UUID, UUID> openPrivateChannels = CacheBuilder.newBuilder()
            .expireAfterWrite(5L, TimeUnit.MINUTES)
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .build();

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
     *
     * @param originalMessage The original content of the message
     * @param channel         The channel to send the message to
     * @param player          The player who sent the message
     */
    public void sendMessage(String originalMessage, ChatChannel channel, CytosisPlayer player) {
        Component channelComponent = Component.empty();
        if (channel != ChatChannel.ALL) {
            channelComponent = channel.getPrefix();
        }
        if (channel == ChatChannel.PRIVATE_MESSAGE) {
            handlePrivateMessage(originalMessage, player);
        }

        Component message = Component.text("")
                .append(channelComponent)
                .append(Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getPrefix())
                .append(Component.text(player.getUsername(), (Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getTeamColor())))
                .append(Component.text(":", Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor()))
                .appendSpace()
                .append(Component.text(originalMessage, Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor()));
        if (channel == ChatChannel.ALL) {
            //todo: this may want to become instance based
            Cytosis.getOnlinePlayers().forEach((p) -> {
                if (!Cytosis.GSON.fromJson(Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.IGNORED_CHAT_CHANNELS), JsonObject.class).get(channel.name()).getAsBoolean())
                    p.sendMessage(message);
            });
            return;
        }
        Cytosis.getNatsManager().sendChatMessage(new ChatMessage(null, channel, JSONComponentSerializer.json().serialize(message), null));
    }

    public void handlePrivateMessage(String message, CytosisPlayer player) {
        if (!openPrivateChannels.asMap().containsKey(player.getUuid())) {
            player.setChatChannel(ChatChannel.ALL);
            player.sendMessage(MM."<red>Your active conversation has expired, so you were put in the ALL channel.");
            return;
        }

        UUID uuid = openPrivateChannels.getIfPresent(player.getUuid());
        PlayerRank recipientRank = Cytosis.getRankManager().getPlayerRank(uuid).orElseThrow();
        Component recipient = recipientRank.getPrefix().append(Component.text(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid), recipientRank.getTeamColor()));
//
        Component component = MM."<dark_aqua>From <reset>".append(player.getRank().getPrefix().append(MM."\{player.getUsername()}")).append(MM."<dark_aqua> » ").append(Component.text(message, NamedTextColor.WHITE));
        Cytosis.getDatabaseManager().getMysqlDatabase().addPlayerMessage(player.getUuid(), uuid, message);
        Cytosis.getNatsManager().sendChatMessage(new ChatMessage(List.of(uuid), ChatChannel.PRIVATE_MESSAGE, JSONComponentSerializer.json().serialize(component), player.getUuid()));
        player.sendMessage(MM."<dark_aqua>To <reset>".append(recipient).append(MM."<dark_aqua> » ").append(Component.text(message, NamedTextColor.WHITE)));
    }

    public void openPrivateMessage(CytosisPlayer player, UUID uuid) {
        openPrivateChannels.put(player.getUuid(), uuid);
    }

    public boolean hasOpenPrivateChannel(CytosisPlayer player, UUID uuid) {
        return openPrivateChannels.getIfPresent(player.getUuid()) == uuid;
    }

    public boolean hasOpenPrivateChannel(CytosisPlayer player) {
        return openPrivateChannels.getIfPresent(player.getUuid()) != null;
    }
}