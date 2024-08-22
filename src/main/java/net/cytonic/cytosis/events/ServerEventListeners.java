package net.cytonic.cytosis.events;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.server.TPSCommand;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.CytosisPreferences;
import net.cytonic.cytosis.data.enums.NPCInteractType;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.npcs.NPC;
import net.cytonic.cytosis.utils.MessageUtils;
import net.cytonic.enums.ChatChannel;
import net.cytonic.enums.KickReason;
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

            // load things as easily as possible
            Cytosis.getFriendManager().loadFriends(player.getUuid());
            Cytosis.getPreferenceManager().loadPlayerPreferences(player.getUuid());
        })));

        Logger.info("Registering player spawn event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-spawn", false, 1, PlayerSpawnEvent.class, (event -> {
            final Player player = event.getPlayer();
            Cytosis.getDatabaseManager().getMysqlDatabase().isBanned(event.getPlayer().getUuid()).whenComplete((data, throwable) -> {
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
            Logger.info(STR."\{player.getUsername()} (\{player.getUuid()}) joined with the ip: \{player.getPlayerConnection().getServerAddress()}");
            Cytosis.getDatabaseManager().getMysqlDatabase().logPlayerJoin(player.getUuid(), player.getPlayerConnection().getRemoteAddress());
            player.setGameMode(GameMode.ADVENTURE);
            Cytosis.getSideboardManager().addPlayer(player);
            Cytosis.getPlayerListManager().setupPlayer(player);
            Cytosis.getRankManager().addPlayer(player);
            if (Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.VANISHED)) {
                Cytosis.getVanishManager().enableVanish(player);
            }
        })));

        Logger.info("Registering player chat event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-chat", false, 1, PlayerChatEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setCancelled(true);
            Cytosis.getDatabaseManager().getMysqlDatabase().isMuted(player.getUuid()).whenComplete((isMuted, throwable) -> {
                if (throwable != null) {
                    Logger.error("An error occurred whilst checking if the player is muted!", throwable);
                    return;
                }
                if (!isMuted) {
                    Cytosis.getDatabaseManager().getMysqlDatabase().addChat(player.getUuid(), event.getMessage());
                    String originalMessage = event.getMessage();
                    ChatChannel channel = Cytosis.getChatManager().getChannel(player.getUuid());
                    if (player.hasPermission(STR."cytonic.chat.\{channel.name().toLowerCase()}") || channel == ChatChannel.ALL) {
                        Cytosis.getChatManager().sendMessage(originalMessage, channel, player);
                    } else {
                        player.sendMessage(MM."<red>Whoops! It looks like you can't chat in the \{channel.name().toLowerCase()} channel. \uD83E\uDD14");
                        Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ALL);
                    }
                    return;
                }
                player.sendMessage(MM."<red>Whoops! You're currently muted.");
            });
        }));

        Logger.info("Registering player disconnect event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-disconnect", false, 1, PlayerDisconnectEvent.class, event -> {
            final Player player = event.getPlayer();
            Cytosis.getRankManager().removePlayer(player);
            Cytosis.getSideboardManager().removePlayer(player);
            Cytosis.getFriendManager().unloadPlayer(player.getUuid());
            if (Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.VANISHED)) {
                Cytosis.getVanishManager().disableVanish(player);
            }
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
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:tps-check", false, 1, ServerTickMonitorEvent.class, (event -> TPSCommand.getLastTick().set(event.getTickMonitor()))));
    }
}