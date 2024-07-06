package net.cytonic.cytosis.events;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.TPSCommand;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.NPCInteractType;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.npcs.NPC;
import net.cytonic.cytosis.utils.MessageUtils;
import net.cytonic.enums.ChatChannel;
import net.cytonic.enums.KickReason;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerTickMonitorEvent;

import java.util.Optional;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A class that registers Cytosis required server events
 */
public final class ServerEventListeners {

    /**
     * Default constructor
     */
    private ServerEventListeners() {
        // do nothing
    }

    /**
     * Adds Cytosis required server events
     */
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
                    Logger.error("An error occurred whilst checking if the player is banned!", throwable);
                    player.kick(MM."<red>An error occurred whilst initiating the login sequence!");
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
            Cytosis.getDatabaseManager().getMysqlDatabase().getServerAlerts(player.getUuid()).whenComplete((value, throwable) -> {
                if (throwable != null) {
                    Logger.error("An error occurred whilst getting a player's server alerts!", throwable);
                } else Cytosis.getCytonicNetwork().getServerAlerts().put(player.getUuid(), value);
            });
            player.setGameMode(GameMode.ADVENTURE);
            Cytosis.getSideboardManager().addPlayer(player);
            player.sendPlayerListHeaderAndFooter(MM."<aqua><bold>CytonicMC", MM."<aqua>mc.cytonic.net");
            Cytosis.getPlayerListManager().setupPlayer(player);
            Cytosis.getPreferenceManager().loadPlayerPreferences(player.getUuid());
        })));

        Logger.info("Registering player chat event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-chat", false, 1, PlayerChatEvent.class, event -> {
            final Player player = event.getPlayer();
            Cytosis.getDatabaseManager().getMysqlDatabase().addChat(player.getUuid(), event.getMessage());
            event.setCancelled(true);
            String originalMessage = event.getMessage();
            ChatChannel channel = Cytosis.getChatManager().getChannel(player.getUuid());
            switch (channel) {
                case STAFF, MOD, ADMIN:
                    if (player.hasPermission(STR."cytonic.chat.\{channel.name().toLowerCase()}")) {
                        sendMessage(originalMessage, channel, player);
                    } else {
                        player.sendMessage(MM."Whoops! It looks like you can't chat in the \{channel.name().toLowerCase()} channel. \uD83E\uDD14");
                        Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ALL);
                    }
                    break;
                case ALL:
                    sendMessage(originalMessage, ChatChannel.ALL, player);
                    break;
            }
        }));

        Logger.info("Registering player disconnect event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-disconnect", false, 1, PlayerDisconnectEvent.class, event -> {
            final Player player = event.getPlayer();
            Cytosis.getRankManager().removePlayer(player);
            Cytosis.getSideboardManager().removePlayer(player);
            Cytosis.getCytonicNetwork().getServerAlerts().remove(player.getUuid());
        }));

        Logger.info("Registering interact events.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-attack", false, 1, EntityAttackEvent.class, event -> {
            if (event.getEntity() instanceof Player player) {
                Optional<NPC> optional = Cytosis.getNpcManager().findNPC(event.getTarget().getUuid());
                if (optional.isPresent() && optional.get() == event.getTarget()) {
                    NPC npc = optional.get();
                    npc.getActions().forEach((action) -> action.execute(npc, NPCInteractType.ATTACK, player));
                }
            }
        }));
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-interact", false, 1, PlayerEntityInteractEvent.class, event -> {
            Optional<NPC> optional = Cytosis.getNpcManager().findNPC(event.getTarget().getUuid());
            if (optional.isPresent() && optional.get() == event.getTarget() && event.getHand() == Player.Hand.MAIN) {
                NPC npc = optional.get();
                npc.getActions().forEach((action) -> action.execute(npc, NPCInteractType.INTERACT, event.getPlayer()));
            }
        }));
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:tps-check", false, 1, ServerTickMonitorEvent.class, (event -> {
            TPSCommand.getLastTick().set(event.getTickMonitor());
        })));
    }

    private static void sendMessage(String originalMessage, ChatChannel channel, Player player) {
        if (!originalMessage.contains("|")) {
            if (channel != ChatChannel.ALL) {
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
    }
}