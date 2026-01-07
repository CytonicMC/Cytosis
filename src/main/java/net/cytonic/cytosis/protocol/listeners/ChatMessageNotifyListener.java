package net.cytonic.cytosis.protocol.listeners;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.errorprone.annotations.Keep;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.sound.SoundEvent;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.protocol.Notifiable;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.ChatMessageProtocolObject;
import net.cytonic.protocol.objects.ChatMessageProtocolObject.Packet;

@Keep
public class ChatMessageNotifyListener implements Notifiable<ChatMessageProtocolObject.Packet> {

    @Override
    public ProtocolObject<Packet, ?> getProtocolObject() {
        return new ChatMessageProtocolObject();
    }

    @Override
    public void onMessage(Packet message) {
        ChatChannel channel = ChatChannel.valueOf(message.channel());

        Component component = message.message().getComponent();

        Set<UUID> recipients;
        if (channel.isSupportsSelectiveRecipients()) {
            recipients = message.recipients();
        } else {
            recipients = Cytosis.getOnlinePlayers().stream().map(CytosisPlayer::getUuid).collect(Collectors.toSet());
        }

        assert recipients != null;
        if (recipients.isEmpty()) return;

        Sound sound = Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, .7f, 1.0F);
        recipients.forEach(uuid -> Cytosis.getPlayer(uuid).ifPresent(player -> {
            if (!player.canReceiveFromChannel(channel)) return;
            if (player.getPreference(CytosisNamespaces.IGNORED_CHAT_CHANNELS).getForChannel(channel)) return;

            if (player.getPreference(CytosisPreferences.CHAT_MESSAGE_PING)) {
                player.playSound(sound);
            }
            player.sendMessage(component);
            if (channel == ChatChannel.PRIVATE_MESSAGE) {
                Cytosis.get(ChatManager.class).openPrivateMessage(player, message.sender());
            }
        }));
    }
}
