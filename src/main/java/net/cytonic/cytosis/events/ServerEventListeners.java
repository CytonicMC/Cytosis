package net.cytonic.cytosis.events;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.NPCInteractType;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.npcs.NPC;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.enums.ChatChannel;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A class that registers Cytosis required server events
 */
@NoArgsConstructor
public final class ServerEventListeners {

    /**
     * Adds Cytosis required server events
     */
    @SuppressWarnings("unchecked")
    public static void initServerEvents() {
        Logger.info("Registering player configuration event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-configuration", true, 1, AsyncPlayerConfigurationEvent.class, (event -> {
            final Player player = event.getPlayer();
            if (!Cytosis.getFlags().contains("--no-instance"))
                event.setSpawningInstance(Cytosis.getDefaultInstance());
            player.setRespawnPoint(CytosisSettings.SERVER_SPAWN_POS);

            // load things as easily as possible
            Cytosis.getFriendManager().loadFriends(player.getUuid());
            Cytosis.getPreferenceManager().loadPlayerPreferences(player.getUuid());
        })));

        Logger.info("Registering player spawn event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-spawn", false, 1, PlayerSpawnEvent.class, (event -> {
            final CytosisPlayer player = (CytosisPlayer) event.getPlayer();
            Logger.info(STR."\{player.getUsername()} (\{player.getUuid()}) joined with the ip: \{player.getPlayerConnection().getServerAddress()}");
            Cytosis.getDatabaseManager().getMysqlDatabase().logPlayerJoin(player.getUuid(), player.getPlayerConnection().getRemoteAddress());
            player.setGameMode(GameMode.ADVENTURE);
            Cytosis.getDatabaseManager().getMysqlDatabase().addPlayer(player);
            Cytosis.getSideboardManager().addPlayer(player);
            Cytosis.getPlayerListManager().setupPlayer(player);
            Cytosis.getRankManager().addPlayer(player);
            Cytosis.getCommandHandler().recalculateCommands(player);
            if (Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.VANISHED)) {
                player.setVanished(true);
            }
            for (CytosisPlayer p : Cytosis.getOnlinePlayers()) {
                if (p.isVanished()) p.setVanished(true);
            }
        })));

        Logger.info("Registering player chat event.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-chat", false, 1, PlayerChatEvent.class, event -> {
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
            final CytosisPlayer player = (CytosisPlayer) event.getPlayer();
            Cytosis.getSideboardManager().removePlayer(player);
            Cytosis.getFriendManager().unloadPlayer(player.getUuid());
            if (Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.VANISHED)) {
                Cytosis.getVanishManager().disableVanish(player);
            }
        }));

        Logger.info("Registering interact events.");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-attack", false, 1, EntityAttackEvent.class, event -> {
            if (event.getEntity() instanceof CytosisPlayer player) {
                Optional<NPC> optional = Cytosis.getNpcManager().findNPC(event.getTarget().getUuid());
                if (optional.isPresent() && optional.get() == event.getTarget()) {
                    NPC npc = optional.get();
                    npc.getActions().forEach((action) -> action.execute(npc, NPCInteractType.ATTACK, player));
                }
            }
        }));
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:player-interact", false, 1, PlayerEntityInteractEvent.class, event -> {
            Optional<NPC> optional = Cytosis.getNpcManager().findNPC(event.getTarget().getUuid());
            if (optional.isPresent() && optional.get() == event.getTarget() && event.getHand() == PlayerHand.MAIN) {
                NPC npc = optional.get();
                npc.getActions().forEach((action) -> action.execute(npc, NPCInteractType.INTERACT, event.getPlayer()));
            }
        }));
        Logger.info("Starting the Block Placement Rules!");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:block-placement", false, 100, PlayerBlockPlaceEvent.class, event -> {
            if (event.getPlayer() instanceof CytosisPlayer player) {
                //todo: add a preference to disable block updates
                event.setDoBlockUpdates(true);
            } else throw new IllegalStateException("Invalid player object");
        }));

        Logger.info("Registering item events");
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:item-drop", false, 1, ItemDropEvent.class, (event -> {
            final Player player = event.getPlayer();
            ItemStack droppedItem = event.getItemStack();

            Pos playerPos = player.getPosition();
            ItemEntity itemEntity = new ItemEntity(droppedItem);
            itemEntity.setPickupDelay(Duration.of(2000, TimeUnit.MILLISECOND));
            itemEntity.setInstance(player.getInstance(), playerPos.withY(y -> y + 1.5));
            Vec velocity = playerPos.direction().mul(6);
            itemEntity.setVelocity(velocity);
        })));
        Cytosis.getEventHandler().registerListener(new EventListener<>("core:item-pickup", false, 1, PickupItemEvent.class, (event -> {
            final Entity entity = event.getLivingEntity();
            if (entity instanceof Player) {
                // Cancel event if player does not have enough inventory space
                final ItemStack itemStack = event.getItemEntity().getItemStack();
                event.setCancelled(!((Player) entity).getInventory().addItemStack(itemStack));
            }
        })));

        Cytosis.getEventHandler().registerListener(new EventListener<>("core:vanish-packet-sniper", true, 0, PlayerPacketOutEvent.class, (e -> {
            if (!(e.getPacket() instanceof EntityMetaDataPacket packet)) return;
            if (!((CytosisPlayer) e.getPlayer()).isStaff()) return;
            if (!Cytosis.getVanishManager().getVanishedEntityIds().contains(packet.entityId())) return;

            Map<Integer, Metadata.Entry<?>> entries = new HashMap<>(packet.entries());

            byte bitmask = 0;
            if (entries.containsKey(0)) {
                bitmask = ((Metadata.Entry<Byte>) entries.get(0)).value();
            }
            if ((bitmask & 0x40) == 0x40 && (bitmask & 0x20) == 0x20) {
                return; // don't need to modify (also prevents a stackoverflow)
            }
            e.setCancelled(true);
            bitmask |= 0x20 | 0x40;
            entries.put(0, Metadata.Byte(bitmask));
            Cytosis.getOnlinePlayers().forEach(p -> {
                if (!p.isStaff()) return;
                p.sendPacket(new EntityMetaDataPacket(packet.entityId(), entries));
            });
        })));
    }
}