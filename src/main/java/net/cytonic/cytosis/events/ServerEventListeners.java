package net.cytonic.cytosis.events;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.KickReason;
import net.cytonic.cytosis.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

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
            Cytosis.getDatabaseManager().getMysqlDatabase().isBanned(event.getPlayer().getUuid()).whenComplete((data, throwable) -> {
                final Player player = event.getPlayer();
                if (throwable != null) {
                    Logger.error("An error occoured whilst checking if the player is banned!", throwable);
                    player.kick(MM."<red>An error occured whilst initiating the login sequence!");
                    return;
                }

                if (data.isBanned()) {
                    Cytosis.getMessagingManager().getRabbitMQ().kickPlayer(player, KickReason.BANNED, MessageUtils.formatBanMessage(data));
                    return;
                }

                Logger.info(STR."\{event.getPlayer().getUsername()} (\{event.getPlayer().getUuid()}) joined with the ip: \{player.getPlayerConnection().getServerAddress()}");
                Cytosis.getDatabaseManager().getMysqlDatabase().addPlayer(player);
                Cytosis.getRankManager().addPlayer(player);
            });
            final Player player = event.getPlayer();
            Logger.info(STR."\{player.getUsername()} (\{player.getUuid()}) joined with the ip: \{player.getPlayerConnection().getServerAddress()}");
            Cytosis.getDatabaseManager().getMysqlDatabase().logPlayerJoin(player.getUuid(), player.getPlayerConnection().getRemoteAddress());
            Cytosis.getRankManager().addPlayer(player);
            Cytosis.getDatabaseManager().getMysqlDatabase().getChatChannel(player.getUuid()).whenComplete(((chatChannel, throwable) -> {
                if (throwable != null) {
                    Logger.error("An error occurred whilst getting a player's chat channel!", throwable);
                } else Cytosis.getChatManager().setChannel(player.getUuid(), chatChannel);
            }));
        })));

        Logger.info("Registering player chat event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-chat", false, 1, PlayerChatEvent.class, event -> {
            final Player player = event.getPlayer();
            if (CytosisSettings.LOG_PLAYER_CHAT)
                Cytosis.getDatabaseManager().getMysqlDatabase().addChat(player.getUuid(), event.getMessage());
            event.setCancelled(true);
            String originalMessage = event.getMessage();
            if (!originalMessage.contains("|")) {
                if (Cytosis.getChatManager().getChannel(player.getUuid()) != ChatChannel.ALL) {
                    ChatChannel channel = Cytosis.getChatManager().getChannel(player.getUuid());
                    Component message = Component.text("")
                            .append(channel.getPrefix())
                            .append(Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getPrefix())
                            .appendSpace()
                            .append(Component.text(player.getUsername(), (Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getTeamColor())))
                            .append(Component.text(":", Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor()))
                            .appendSpace()
                            .append(Component.text(originalMessage, Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor()));
                    Cytosis.getChatManager().sendMessageToChannel(message, Cytosis.getChatManager().getChannel(player.getUuid()));
                } else {
                    Component message = Component.text("")
                            .append(Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getPrefix())
                            .appendSpace()
                            .append(Component.text(player.getUsername(), (Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getTeamColor())))
                            .append(Component.text(":", Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor()))
                            .appendSpace()
                            .append(Component.text(originalMessage, Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow().getChatColor()));
                    Cytosis.getOnlinePlayers().forEach((p) -> p.sendMessage(message));
                }
            } else player.sendMessage(MM."<red>Hey you cannot do that!");
            Cytosis.getDatabaseManager().getMysqlDatabase().addChat(player.getUuid(), event.getMessage());
        }));

        Logger.info("Registering player disconnect event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-disconnect", false, 1, PlayerDisconnectEvent.class, event -> {
            final Player player = event.getPlayer();
            Cytosis.getRankManager().removePlayer(player);
        }));
    }
}