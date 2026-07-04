package net.cytonic.cytosis.managers;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.ChatMessage;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;
import net.cytonic.protocol.impl.notify.ChatMessageNotifyPacket;

/**
 * This class handles chat messages and channels
 */
@SuppressWarnings("unused")
@NoArgsConstructor
@CytosisComponent(dependsOn = {PreferenceManager.class})
public class ChatManager implements Bootstrappable {

    public static final Map<String, String> EMOJIS = Map.ofEntries(
        Map.entry(":3", "<#f2a7f5>•⩊•</#f2a7f5>"),
        Map.entry(":skull:", "<red>☠</red>"),
        Map.entry("^_^", "<#ecce75>(˶ˆᗜˆ˵)</#ecce75>"),
        Map.entry(">:(", "<red>︵ヽ(`▭´)ﾉ︵</red>"),
        Map.entry("<3", "<#c361eb>⸜(｡˃ ᵕ ˂ )⸝♡</#c361eb>"),
        Map.entry("</3", "<red>\uD83D\uDC94</red>"),
        Map.entry("._.", "⊙﹏⊙"),
        Map.entry(";-;", "<#4354f4>(ￗ﹏ￗ)</#4354f4>"),
        Map.entry(":yoo:", "(╭☞⚆ᗜ⚆)╭☞"),
        Map.entry(":shrug:", "¯\\(ツ)/¯"),
        Map.entry(":yay:", "⸜( ˙˘˙)⸝"),
        Map.entry(":+1:", "(ദി˙ᗜ˙)"),
        Map.entry(":rip:", "<red>\uD83E\uDEA6</red>"),
        Map.entry(":cat:", "ᓚᘏᗢ"),
        Map.entry(":)", "❀◕ ‿ ◕❀")
    );
    private final Cache<UUID, UUID> openPrivateChannels = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(5))
        .expireAfterAccess(Duration.ofMinutes(5))
        .build();
    private PreferenceManager preferenceManager;

    @Override
    public void init() {
        this.preferenceManager = Cytosis.get(PreferenceManager.class);
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
        originalMessage = Msg.stripTags(originalMessage);
        if (channel == ChatChannel.ALL) {
            Cytosis.getServer().chatService().handleAllChat(player, originalMessage);
            return;
        }

        String msg = translateEmojis(originalMessage, player.getTrueRank());
        if (channel == ChatChannel.PRIVATE_MESSAGE) {
            handlePrivateMessage(msg, player, null);
            return;
        }

        if (!player.canSendToChannel(channel)) {
            player.sendMessage(Msg.whoops("You cannot currently send messages on the <gold>%s</gold> channel.",
                channel.name()));
            return;
        }
        String color = player.getTrueRank().getChatColor();
        msg = color + ": " + msg;

        logMessage(null, player.getUuid(), msg, channel);

        Component message = Component.text("");

        Component chan = channel.getPrefix();
        if (channel.isShouldDeanonymize()) {
            message = message.append(chan).append(player.trueFormattedName()).append(Msg.mm(msg));
        } else {
            message = message.append(chan).append(player.formattedName()).append(Msg.mm(msg));
        }

        Set<UUID> recipients = null;
        if (channel.isSupportsSelectiveRecipients()) {
            assert channel.getRecipientFunction() != null;
            recipients = channel.getRecipientFunction().apply(player);
        }

        new ChatMessageNotifyPacket.Packet(recipients, channel, message, player.getUuid()).publish();
    }

    public void handlePrivateMessage(String message, CytosisPlayer player, @Nullable UUID recipientId) {

        if (recipientId == null) {
            recipientId = openPrivateChannels.getIfPresent(player.getUuid());
            if (recipientId == null) {
                player.setChatChannel(ChatChannel.ALL);
                player.sendMessage(
                    Msg.red("Your active conversation has expired, so you were put in the ALL channel."));
                return;
            }
        }

        CytonicNetwork cn = Cytosis.get(CytonicNetwork.class);

        logMessage(recipientId, player.getUuid(), message, ChatChannel.PRIVATE_MESSAGE);

        String recipient = cn.getMiniName(recipientId);
        String sender = cn.getTrueMiniName(player.getUuid());

        Component component = Msg.darkAqua("From %s » </dark_aqua>%s", sender, message);
        new ChatMessageNotifyPacket.Packet(Set.of(recipientId), ChatChannel.PRIVATE_MESSAGE, component,
            player.getUuid()).publish();
        player.sendMessage(Msg.darkAqua("To %s » </dark_aqua>%s", recipient, message));
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

    public void logMessage(@Nullable UUID recipient, UUID sender, String message, ChatChannel channel) {
        Thread.ofVirtual().start(() -> {
            ChatMessage msg = new ChatMessage();
            msg.setChannel(channel);
            msg.setSender(sender);
            msg.setContent(message);
            msg.setSentAt(Instant.now());
            msg.setRecipient(recipient);
            msg.save();
        });
    }

    public String translateEmojis(String message, PlayerRank rank) {
        if (!rank.isHigherOrEqualTo(PlayerRank.SYNAPSE)) return message;

        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < message.length()) {
            String matched = null;
            String replacement = null;

            for (Map.Entry<String, String> entry : EMOJIS.entrySet()) {
                String key = entry.getKey();
                if (message.startsWith(key, i)) {
                    matched = key;
                    replacement = entry.getValue();
                    break;
                }
            }

            if (matched != null) {
                result.append(replacement);
                i += matched.length();
            } else {
                result.append(message.charAt(i));
                i++;
            }
        }

        return result.toString();
    }
}