package net.cytonic.cytosis.data.packet.listeners;

import java.util.UUID;

import lombok.NoArgsConstructor;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.sound.SoundEvent;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.packet.packets.ChatMessagePacket;
import net.cytonic.cytosis.data.packet.packets.PacketHandler;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;

@CytosisComponent
@NoArgsConstructor
public class ChatMessageListener {

    @PacketHandler(subject = Subjects.CHAT_MESSAGE)
    private void handleChatMessage(ChatMessagePacket packet) {
        Component component = packet.getMessage().getComponent();
        ChatChannel channel = packet.getChannel();
        switch (channel) {
            case PRIVATE_MESSAGE -> {
                if (packet.getRecipients() == null || packet.getRecipients().isEmpty()) {
                    return;
                }
                packet.getRecipients().forEach(recipient ->
                    Cytosis.getPlayer(recipient).ifPresent(player -> {
                        if (player.getPreference(CytosisPreferences.CHAT_MESSAGE_PING)) {
                            player.playSound(
                                Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, .7f, 1.0F));
                        }
                        player.sendMessage(component);
                        Cytosis.get(ChatManager.class).openPrivateMessage(player, packet.getSender());
                    }));
                return;
            }

            case INTERNAL_MESSAGE -> {
                if (packet.getRecipients() == null || packet.getRecipients().isEmpty()) {
                    return;
                }
                for (UUID uuid : packet.getRecipients()) {
                    Cytosis.getPlayer(uuid).ifPresent(player -> player.sendMessage(component));
                }
                return;
            }
        }
        if (!channel.isSupportsSelectiveRecipients()) {
            Cytosis.getOnlinePlayers().forEach(player -> {
                if (player.canUseChannel(channel) && !player.getPreference(
                    CytosisNamespaces.IGNORED_CHAT_CHANNELS).getForChannel(channel)) {
                    if (player.getPreference(CytosisPreferences.CHAT_MESSAGE_PING)) {
                        player.playSound(
                            Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, .7f, 1.0F));
                    }
                    player.sendMessage(component);
                }
            });
        }
    }
}
