package net.cytonic.cytosis.managers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.DatabaseManager;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.ChatMessage;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;

/**
 * This class handles chat messages and channels
 */
@SuppressWarnings("unused")
@NoArgsConstructor
@CytosisComponent(dependsOn = {PreferenceManager.class, NatsManager.class})
public class ChatManager implements Bootstrappable {

    private final Cache<UUID, UUID> openPrivateChannels = CacheBuilder.newBuilder()
        .expireAfterWrite(5L, TimeUnit.MINUTES)
        .expireAfterAccess(5L, TimeUnit.MINUTES).build();

    private PreferenceManager preferenceManager;
    private NatsManager natsManager;

    @Override
    public void init() {
        this.preferenceManager = Cytosis.CONTEXT.getComponent(PreferenceManager.class);
        this.natsManager = Cytosis.CONTEXT.getComponent(NatsManager.class);
    }

    /**
     * Sets a specified players chat channel
     *
     * @param uuid    The player to set
     * @param channel The channel to set
     */
    public void setChannel(UUID uuid, ChatChannel channel) {
        preferenceManager.updatePlayerPreference(uuid, CytosisNamespaces.CHAT_CHANNEL, channel);
    }

    /**
     * Gets the player's chat channel
     *
     * @param uuid The player's uuid
     * @return the player's currently selected chat channel
     */
    public ChatChannel getChannel(UUID uuid) {
        return preferenceManager.getPlayerPreference(uuid, CytosisPreferences.CHAT_CHANNEL);
    }

    /**
     * Sends a message out to redis.
     *
     * @param originalMessage The original content of the message
     * @param channel         The channel to send the message to
     * @param player          The player who sent the message
     */
    public void sendMessage(String originalMessage, ChatChannel channel, CytosisPlayer player) {
        Component channelComponent;
        if (channel != ChatChannel.ALL) {
            channelComponent = channel.getPrefix();
        } else {
            channelComponent = Component.empty();
        }
        if (channel == ChatChannel.PRIVATE_MESSAGE) {
            handlePrivateMessage(originalMessage, player);
        }

        Component message = Component.text("");
        if (channel.isShouldDeanonymize()) {
            message = message.append(channelComponent).append(player.trueFormattedName())
                .append(Component.text(":", player.getTrueRank().getChatColor())).appendSpace()
                .append(Component.text(originalMessage, player.getTrueRank().getChatColor()));
        } else {
            message = message.append(channelComponent).append(player.formattedName())
                .append(Component.text(":", player.getRank().getChatColor())).appendSpace()
                .append(Component.text(originalMessage, player.getRank().getChatColor()));
        }

        if (channel == ChatChannel.ALL) {
            //todo: this may want to become instance based
            Component finalMessage = message;
            Cytosis.getOnlinePlayers().forEach((p) -> {
                // todo: admins see real name?
                if (player.getUuid().equals(p.getUuid())) {
                    p.sendMessage(channelComponent.append(player.trueFormattedName())
                        .append(Component.text(":", player.getTrueRank().getChatColor()))
                        .appendSpace()
                        .append(Component.text(originalMessage, player.getTrueRank()
                            .getChatColor())));
                    return;
                }
                if (!p.getPreference(CytosisNamespaces.IGNORED_CHAT_CHANNELS).getForChannel(channel)) {
                    p.sendMessage(finalMessage);
                }
            });
            return;
        }
        natsManager.sendChatMessage(new ChatMessage(null, channel, Msg.toJson(message), player.getUuid()));
    }

    public void handlePrivateMessage(String message, CytosisPlayer player) {
        if (!openPrivateChannels.asMap().containsKey(player.getUuid())) {
            player.setChatChannel(ChatChannel.ALL);
            player.sendMessage(Msg.red("Your active conversation has expired, so you were put in the ALL channel."));
            return;
        }

        UUID uuid = openPrivateChannels.getIfPresent(player.getUuid());
        PlayerRank recipientRank = Cytosis.CONTEXT.getComponent(RankManager.class).getPlayerRank(uuid).orElseThrow();

        Component recipient = recipientRank.getPrefix()
            .append(Component.text(Cytosis.CONTEXT.getComponent(CytonicNetwork.class).getLifetimePlayers()
                .getByKey(uuid), recipientRank.getTeamColor()));

        Component component = Msg.mm("<dark_aqua>From <reset>")
            .append(player.getTrueRank().getPrefix().append(Msg.mm(player.getTrueUsername())))
            .append(Msg.mm("<dark_aqua> » "))
            .append(Component.text(message, NamedTextColor.WHITE));
        Cytosis.CONTEXT.getComponent(MysqlDatabase.class).addPlayerMessage(player.getUuid(), uuid, message);
        natsManager
            .sendChatMessage(new ChatMessage(List.of(uuid), ChatChannel.PRIVATE_MESSAGE, JSONComponentSerializer.json()
                .serialize(component), player.getUuid()));
        player.sendMessage(Msg.mm("<dark_aqua>To <reset>").append(recipient).append(Msg.mm("<dark_aqua> » "))
            .append(Component.text(message, NamedTextColor.WHITE)));
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