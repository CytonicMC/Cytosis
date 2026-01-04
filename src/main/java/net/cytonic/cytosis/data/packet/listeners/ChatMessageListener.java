package net.cytonic.cytosis.data.packet.listeners;

import java.util.List;
import java.util.UUID;

import lombok.NoArgsConstructor;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.sound.SoundEvent;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.packet.packets.ChatMessagePacket;
import net.cytonic.cytosis.data.packet.utils.PacketHandler;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;

@CytosisComponent
@NoArgsConstructor
public class ChatMessageListener {

    @PacketHandler(subject = Subjects.CHAT_MESSAGE)
    private void handleChatMessage(ChatMessagePacket packet) {
        ChatChannel channel = packet.getChannel();

        Component component = packet.getMessage().getComponent();

        List<UUID> recipients;
        if (channel.isSupportsSelectiveRecipients()) {
            recipients = packet.getRecipients();
        } else {
            recipients = Cytosis.getOnlinePlayers().stream().map(CytosisPlayer::getUuid).toList();
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
                Cytosis.get(ChatManager.class).openPrivateMessage(player, packet.getSender());
            }
        }));
    }
}
