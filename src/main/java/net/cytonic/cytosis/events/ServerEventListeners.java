package net.cytonic.cytosis.events;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;

public class ServerEventListeners {

    public static void initServerEvents() {
        Logger.info("Registering player configuration event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-configuration", true, 1, AsyncPlayerConfigurationEvent.class, (event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(Cytosis.getDefaultInstance());
            player.setRespawnPoint(CytosisSettings.SERVER_SPAWN_POS);
        })));

        Logger.info("Registering player spawn event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-spawn", false, 1, PlayerSpawnEvent.class, (event -> {
            final Player player = event.getPlayer();
            Logger.info(STR."\{player.getUsername()} (\{player.getUuid()}) joined with the ip: \{player.getPlayerConnection().getServerAddress()}");
            Cytosis.getDatabaseManager().getDatabase().logPlayerJoin(player.getUuid(), player.getPlayerConnection().getRemoteAddress());
            Cytosis.getRankManager().addPlayer(player);
        })));

        Logger.info("Registering player chat event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-chat", false, 1, PlayerChatEvent.class, event -> {
            final Player player = event.getPlayer();
            if (CytosisSettings.LOG_PLAYER_CHAT)
                Cytosis.getDatabaseManager().getDatabase().addChat(player.getUuid(), event.getMessage());
            event.setCancelled(true);
            String originalMessage = event.getMessage();
            if(Cytosis.getChatManager().getChannel(player.getUuid()) != ChatChannel.ALL) {
                ChatChannel channel = Cytosis.getChatManager().getChannel(player.getUuid());
                Component message = Component.text("")
                    .append(channel.getPrefix())
                    .append(Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getPrefix())
                    .append(Component.text(player.getUsername(), (Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor())))
                    .appendSpace()
                    .append(Component.text(originalMessage, NamedTextColor.WHITE));
                    Cytosis.getChatManager().sendMessageToChannel(message, Cytosis.getChatManager().getChannel(player.getUuid()));
                } else {
                    Component message = Component.text("")
                    .append(Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getPrefix())
                    .append(Component.text(player.getUsername(), (Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor())))
                    .appendSpace()
                    .append(Component.text(originalMessage, NamedTextColor.WHITE));
                    Cytosis.getOnlinePlayers().forEach((p) -> {p.sendMessage(message);});
                }
        }));

        Logger.info("Registering player disconnect event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-disconnect", false, 1, PlayerDisconnectEvent.class, event -> {
            final Player player = event.getPlayer();
            Cytosis.getRankManager().removePlayer(player);
        }));
    }

    public static void chatEvent(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if(Cytosis.getChatManager().getChannel(player.getUuid()) != ChatChannel.ALL) {
            ChatChannel channel = Cytosis.getChatManager().getChannel(player.getUuid());
            event.setCancelled(true);
            String originalMessage = event.getMessage();
            Component message = Component.text("")
                    .append(channel.getPrefix())
                    .append(Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getPrefix())
                    .append(Component.text(player.getUsername(), (Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor())))
                    .appendSpace()
                    .append(Component.text(originalMessage, NamedTextColor.WHITE));
            Cytosis.getChatManager().sendMessageToChannel(message, Cytosis.getChatManager().getChannel(player.getUuid()));
        } else {
            event.setCancelled(false);
        }
    }
}