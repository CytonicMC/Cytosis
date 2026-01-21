package net.cytonic.cytosis.events;

import io.opentelemetry.api.common.Attributes;
import lombok.NoArgsConstructor;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandHandler;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.GlobalDatabase;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.enums.NpcInteractType;
import net.cytonic.cytosis.entity.hologram.PlayerHolograms;
import net.cytonic.cytosis.entity.npc.NPC;
import net.cytonic.cytosis.events.api.Async;
import net.cytonic.cytosis.events.api.Listener;
import net.cytonic.cytosis.events.api.Priority;
import net.cytonic.cytosis.events.npcs.NPCInteractEvent;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.managers.NpcManager;
import net.cytonic.cytosis.managers.PlayerListManager;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.cytosis.managers.SideboardManager;
import net.cytonic.cytosis.managers.VanishManager;
import net.cytonic.cytosis.metrics.MetricsManager;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.MetadataPacketBuilder;
import net.cytonic.cytosis.utils.Msg;

/**
 * A class that registers Cytosis required server events
 */
@NoArgsConstructor
@SuppressWarnings("unused")
public final class ServerEventListeners {

    public static double RAW_MSPT = 0;

    @Listener
    @Priority(1)
    private void onPacketIn(PlayerPacketEvent event) {
        if (!(event.getPlayer() instanceof CytosisPlayer player)) return;
        if (event.getPacket() instanceof ClientInteractEntityPacket interactEntityPacket) {
            ClientInteractEntityPacket.Type packetType = interactEntityPacket.type();
            if (packetType instanceof ClientInteractEntityPacket.Interact) {
                return;
            }
            if (!(packetType instanceof ClientInteractEntityPacket.Attack)) {
                if (!(packetType instanceof ClientInteractEntityPacket.InteractAt interactAt)) {
                    return;
                }
                if (interactAt.hand().ordinal() != 0) return;
            }
            NPC npc = Cytosis.get(NpcManager.class).getNPC(player, interactEntityPacket.targetId());
            if (npc == null) return;
            NpcInteractType interactType =
                interactEntityPacket.type() instanceof ClientInteractEntityPacket.Attack ? NpcInteractType.ATTACK
                    : NpcInteractType.INTERACT;
            NPCInteractEvent clickEvent = new NPCInteractEvent(player, interactType, npc);
            EventDispatcher.callCancellable(clickEvent, () -> npc.onClick(clickEvent));
        }
    }

    @Listener
    @Priority(100)
    private void onBlockPlace(PlayerBlockPlaceEvent event) {
        if (event.getPlayer() instanceof CytosisPlayer player) {
            //todo: add a preference to disable block updates
            event.setDoBlockUpdates(true);
        } else {
            throw new IllegalStateException("Invalid player object");
        }
    }

    @Listener
    @Async
    private void onTick(ServerTickMonitorEvent event) {
        RAW_MSPT = event.getTickMonitor().getTickTime();
    }

    @Listener
    @Priority(0)
    @Async
    @SuppressWarnings({"UnstableApiUsage"})
    private void onPacketOut(PlayerPacketOutEvent e) {
        if (!(e.getPacket() instanceof EntityMetaDataPacket packet)) return;
        if (!((CytosisPlayer) e.getPlayer()).isStaff()) return;
        if (!Cytosis.get(VanishManager.class).getVanished().containsValue(packet.entityId())) return;

        MetadataPacketBuilder builder = MetadataPacketBuilder.builder(packet);

        if (builder.isGlowing() && builder.isInvisible()) {
            return; // don't need to modify (also prevents a stackoverflow)
        }
        e.setCancelled(true);

        SendablePacket toSend = builder.setGlowing(true).setInvisible(true).build();

        Cytosis.getOnlinePlayers().forEach(p -> {
            if (!p.isStaff()) return;
            p.sendPacket(toSend);
        });
    }

    @Priority(1)
    @Listener
    private void onConfig(AsyncPlayerConfigurationEvent event) {
        final Player player = event.getPlayer();
        if (!Cytosis.CONTEXT.getFlags().contains("--no-instance")) {
            event.setSpawningInstance(Cytosis.get(InstanceContainer.class));
        }
        player.setRespawnPoint(
            Cytosis.get(CytosisSettings.class).getServerConfig().getSpawnPos());

        // load things as easily as possible
        Cytosis.get(FriendManager.class).loadFriends(player.getUuid());
        Cytosis.get(PreferenceManager.class).loadPlayerPreferences(player.getUuid());
    }

    @Listener
    @Priority(1)
    private void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        final CytosisPlayer player = (CytosisPlayer) event.getPlayer();
        Logger.info(
            player.getUsername() + " (" + player.getUuid() + ") joined with the ip: " + player.getPlayerConnection()
                .getRemoteAddress());
        MysqlDatabase db = Cytosis.get(MysqlDatabase.class);
        GlobalDatabase gdb = Cytosis.get(GlobalDatabase.class);

        db.logPlayerJoin(player.getUuid(), player.getPlayerConnection().getRemoteAddress());
        player.setGameMode(GameMode.ADVENTURE);
        gdb.addPlayer(player);
        Cytosis.get(SideboardManager.class).addPlayer(player);
        Cytosis.get(PlayerListManager.class).setupPlayer(player);
        Cytosis.get(RankManager.class).addPlayer(player);
        Cytosis.get(CommandHandler.class).recalculateCommands(player);
        if (player.getPreference(CytosisPreferences.VANISHED)) {
            player.setVanished(true);
        }
        try {
            if (player.getPreference(CytosisPreferences.NICKNAME_DATA) != null) {
                Cytosis.get(NicknameManager.class).loadNickedPlayer(player);
            }
        } catch (Exception e) {
            Logger.error("Failed to load nickname data for " + player.getUsername() + " (" + player.getUuid() + ")", e);
        }
        for (CytosisPlayer p : Cytosis.getOnlinePlayers()) {
            if (p.isVanished()) {
                p.setVanished(true);
            }
        }
        if (!player.hasPlayedBefore() && Cytosis.CONTEXT.isMetricsEnabled()) {
            // add a new player who hasn't played before
            Cytosis.get(MetricsManager.class)
                .addToLongCounter("players.unique", 1, Attributes.empty());
        }
    }

    @Listener
    @Priority(1)
    private void onChat(PlayerChatEvent event) {
        final CytosisPlayer player = (CytosisPlayer) event.getPlayer();
        event.setCancelled(true);
        GlobalDatabase gdb = Cytosis.get(GlobalDatabase.class);
        MysqlDatabase db = Cytosis.get(MysqlDatabase.class);
        gdb.isMuted(player.getUuid()).whenComplete((isMuted, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst checking if the player is muted!", throwable);
                return;
            }
            if (isMuted) {
                player.sendMessage(Msg.whoops("You're currently muted."));
                return;
            }

            ChatManager chatManager = Cytosis.get(ChatManager.class);
            db.addChat(player.getUuid(), event.getRawMessage());
            String originalMessage = event.getRawMessage();
            ChatChannel channel = chatManager.getChannel(player.getUuid());
            if (player.canSendToChannel(channel)) {
                chatManager.sendMessage(originalMessage, channel, player);
            } else {
                player.sendMessage(Msg.whoops("It looks like you can't chat in the " + channel.name()
                    .toLowerCase() + " channel. \uD83E\uDD14"));
                chatManager.setChannel(player.getUuid(), ChatChannel.ALL);
            }

        });
    }

    @Listener
    @Priority(1)
    private void onQuit(PlayerDisconnectEvent event) {
        final CytosisPlayer player = (CytosisPlayer) event.getPlayer();
        Cytosis.get(SideboardManager.class).removePlayer(player);
        Cytosis.get(FriendManager.class).unloadPlayer(player.getUuid());
        if (Cytosis.get(PreferenceManager.class)
            .getPlayerPreference(player.getUuid(), CytosisPreferences.VANISHED)) {
            Cytosis.get(VanishManager.class).disableVanish(player);
        }
        Cytosis.get(NpcManager.class).removePlayer(player);
        PlayerHolograms.removePlayer(player);
    }
}