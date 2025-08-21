package net.cytonic.cytosis.events;

import io.opentelemetry.api.common.Attributes;
import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.enums.NPCInteractType;
import net.cytonic.cytosis.events.api.Async;
import net.cytonic.cytosis.events.api.Listener;
import net.cytonic.cytosis.events.api.Priority;
import net.cytonic.cytosis.events.npcs.NpcInteractEvent;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.npcs.NPC;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.MetadataPacketBuilder;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;

import java.util.Optional;

/**
 * A class that registers Cytosis required server events
 */
@NoArgsConstructor
public final class ServerEventListeners {

    public static double RAW_MSPT = 0;

    @Listener
    @Priority(1)
    private void onInteract(PlayerEntityInteractEvent event) {
        Optional<NPC> optional = Cytosis.getNpcManager().findNPC(event.getTarget().getUuid());
        if (optional.isPresent() && optional.get() == event.getTarget() && event.getHand() == PlayerHand.MAIN) {
            NPC npc = optional.get();
            EventDispatcher.call(new NpcInteractEvent(npc, (CytosisPlayer) event.getPlayer(), npc.getActions()));
            npc.getActions().forEach((action) -> action.execute(npc, NPCInteractType.INTERACT, (CytosisPlayer) event.getPlayer()));
        }
    }

    @Listener
    @Priority(100)
    private void onBlockPlace(PlayerBlockPlaceEvent event) {
        if (event.getPlayer() instanceof CytosisPlayer player) {
            //todo: add a preference to disable block updates
            event.setDoBlockUpdates(true);
        } else throw new IllegalStateException("Invalid player object");
    }

    @Listener
    @Async
    private void onTick(ServerTickMonitorEvent event) {
        RAW_MSPT = event.getTickMonitor().getTickTime();
    }

    @Listener
    @Priority(0)
    @Async
    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    private void onPacketOut(PlayerPacketOutEvent e) {
        if (!(e.getPacket() instanceof EntityMetaDataPacket packet))
            return;
        if (!((CytosisPlayer) e.getPlayer()).isStaff()) return;
        if (!Cytosis.getVanishManager().getVanished().containsValue(packet.entityId())) return;

        MetadataPacketBuilder builder = MetadataPacketBuilder.builder(packet);


        if (builder.isGlowing() && builder.isInvisible()) {
            return; // don't need to modify (also prevents a stackoverflow)
        }
        e.setCancelled(true);

        SendablePacket toSend = builder.setGlowing(true)
                .setInvisible(true)
                .build();


        Cytosis.getOnlinePlayers().forEach(p -> {
            if (!p.isStaff()) return;
            p.sendPacket(toSend);
        });
    }

    @Priority(1)
    @Listener
    private void onConfig(AsyncPlayerConfigurationEvent event) {
        final Player player = event.getPlayer();
        if (!Cytosis.getFlags().contains("--no-instance"))
            event.setSpawningInstance(Cytosis.getDefaultInstance());
        player.setRespawnPoint(CytosisSettings.SERVER_SPAWN_POS);

        // load things as easily as possible
        Cytosis.getFriendManager().loadFriends(player.getUuid());
        Cytosis.getPreferenceManager().loadPlayerPreferences(player.getUuid());
    }

    @Listener
    @Priority(1)
    private void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        final CytosisPlayer player = (CytosisPlayer) event.getPlayer();
        Logger.info(player.getUsername() + " (" + player.getUuid() + ") joined with the ip: " + player.getPlayerConnection().getRemoteAddress());
        Cytosis.getDatabaseManager().getMysqlDatabase().logPlayerJoin(player.getUuid(), player.getPlayerConnection().getRemoteAddress());
        player.setGameMode(GameMode.ADVENTURE);
        Cytosis.getDatabaseManager().getMysqlDatabase().addPlayer(player);
        Cytosis.getSideboardManager().addPlayer(player);
        Cytosis.getPlayerListManager().setupPlayer(player);
        Cytosis.getRankManager().addPlayer(player);
        Cytosis.getCommandHandler().recalculateCommands(player);
        if (player.getPreference(CytosisPreferences.VANISHED)) {
            player.setVanished(true);
        }
        try {
            if (player.getPreference(CytosisPreferences.NICKNAME_DATA) != null) {
                Cytosis.getNicknameManager().loadNickedPlayer(player);
            }
        } catch (Exception e) {
            Logger.error("Failed to load nickname data for " + player.getUsername() + " (" + player.getUuid() + ")", e);
        }
        for (CytosisPlayer p : Cytosis.getOnlinePlayers()) {
            if (p.isVanished()) p.setVanished(true);
        }
        if (!player.hasPlayedBefore() && Cytosis.isMetricsEnabled()) {
            // add a new player who hasn't played before
            Cytosis.getMetricsManager().addToLongCounter("players.unique", 1, Attributes.empty());
        }
    }

    @Listener
    @Priority(1)
    private void onChat(PlayerChatEvent event) {
        final CytosisPlayer player = (CytosisPlayer) event.getPlayer();
        event.setCancelled(true);
        Cytosis.getDatabaseManager().getMysqlDatabase().isMuted(player.getUuid()).whenComplete((isMuted, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst checking if the player is muted!", throwable);
                return;
            }
            if (!isMuted) {
                Cytosis.getDatabaseManager().getMysqlDatabase().addChat(player.getUuid(), event.getRawMessage());
                String originalMessage = event.getRawMessage();
                ChatChannel channel = Cytosis.getChatManager().getChannel(player.getUuid());
                if (player.canUseChannel(channel) || channel == ChatChannel.ALL) {
                    Cytosis.getChatManager().sendMessage(originalMessage, channel, player);
                } else {
                    player.sendMessage(Msg.whoops("It looks like you can't chat in the " + channel.name().toLowerCase() + " channel. \uD83E\uDD14"));
                    Cytosis.getChatManager().setChannel(player.getUuid(), ChatChannel.ALL);
                }
                return;
            }
            player.sendMessage(Msg.whoops("You're currently muted."));
        });
    }

    @Listener
    @Priority(1)
    private void onQuit(PlayerDisconnectEvent event) {
        final CytosisPlayer player = (CytosisPlayer) event.getPlayer();
        Cytosis.getSideboardManager().removePlayer(player);
        Cytosis.getFriendManager().unloadPlayer(player.getUuid());
        if (Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.VANISHED)) {
            Cytosis.getVanishManager().disableVanish(player);
        }
    }

    @Listener
    @Priority(1)
    private void onAttack(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof CytosisPlayer player)) return;
        Optional<NPC> optional = Cytosis.getNpcManager().findNPC(event.getTarget().getUuid());
        if (optional.isPresent() && optional.get() == event.getTarget()) {
            NPC npc = optional.get();
            MinecraftServer.getGlobalEventHandler().call(new NpcInteractEvent(npc, player, npc.getActions()));
            npc.getActions().forEach((action) -> action.execute(npc, NPCInteractType.ATTACK, player));
        }
    }
}