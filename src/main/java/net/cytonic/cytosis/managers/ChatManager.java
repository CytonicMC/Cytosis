package net.cytonic.cytosis.managers;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.EnvironmentDatabase;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.impl.notify.ChatMessageNotifyPacket;

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
        this.preferenceManager = Cytosis.get(PreferenceManager.class);
        this.natsManager = Cytosis.get(NatsManager.class);
    }

    /**
     * Sets a specified players chat channel
     *
     * @param uuid    The player to set
     * @param channel The channel to set
     */
    public void setChannel(UUID uuid, ChatChannel channel) {
        preferenceManager.updatePlayerPreference(uuid, Preferences.CHAT_CHANNEL, channel);
    }

    /**
     * Gets the player's chat channel
     *
     * @param uuid The player's uuid
     * @return the player's currently selected chat channel
     */
    public ChatChannel getChannel(UUID uuid) {
        return preferenceManager.getPlayerPreference(uuid, Preferences.CHAT_CHANNEL);
    }

    /**
     * Sends a message out to redis.
     *
     * @param originalMessage The original content of the message
     * @param channel         The channel to send the message to
     * @param player          The player who sent the message
     */
    public void sendMessage(String originalMessage, ChatChannel channel, CytosisPlayer player) {
        if (!player.canSendToChannel(channel)) {
            player.sendMessage(Msg.whoops("You cannot currently send messages on the <gold>%s</gold> channel.",
                channel.name()));
            return;
        }
        Component channelComponent = channel.getPrefix();
        if (channel == ChatChannel.PRIVATE_MESSAGE) {
            handlePrivateMessage(originalMessage, player);
            return;
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

        Set<UUID> recipients = null;
        if (channel.isSupportsSelectiveRecipients()) {
            assert channel.getRecipientFunction() != null;
            recipients = channel.getRecipientFunction().apply(player);
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
                if (!p.getPreference(Preferences.IGNORED_CHAT_CHANNELS).getForChannel(channel)) {
                    p.sendMessage(finalMessage);
                }
            });
            return;
        }
        new ChatMessageNotifyPacket.Packet(recipients, channel, new JsonComponent(message), player.getUuid()).publish();
    }

    public void handlePrivateMessage(String message, CytosisPlayer player) {
        if (!openPrivateChannels.asMap().containsKey(player.getUuid())) {
            player.setChatChannel(ChatChannel.ALL);
            player.sendMessage(Msg.red("Your active conversation has expired, so you were put in the ALL channel."));
            return;
        }

        UUID uuid = openPrivateChannels.getIfPresent(player.getUuid());
        PlayerRank recipientRank = Cytosis.get(RankManager.class).getPlayerRank(uuid).orElseThrow();

        Component recipient = recipientRank.getPrefix()
            .append(Component.text(Cytosis.get(CytonicNetwork.class).getLifetimePlayers()
                .getByKey(uuid), recipientRank.getTeamColor()));

        Component component = Msg.mm("<dark_aqua>From <reset>")
            .append(player.getTrueRank().getPrefix().append(Msg.mm(player.getTrueUsername())))
            .append(Msg.mm("<dark_aqua> » "))
            .append(Component.text(message, NamedTextColor.WHITE));
        Cytosis.get(EnvironmentDatabase.class).addPlayerMessage(player.getUuid(), uuid, message);
        new ChatMessageNotifyPacket.Packet(Set.of(Objects.requireNonNull(uuid)), ChatChannel.PRIVATE_MESSAGE,
            new JsonComponent(component), player.getUuid()).publish();
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