package net.cytonic.cytosis.events;

import net.minestom.server.event.Event;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.book.EditBookEvent;
import net.minestom.server.event.entity.*;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent;
import net.minestom.server.event.instance.*;
import net.minestom.server.event.inventory.*;
import net.minestom.server.event.item.*;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ClientPingServerEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.event.trait.CancellableEvent;
import java.util.*;

/**
 * EventHandler class is responsible for handling events and managing listeners.
 * It provides methods to register, unregister listeners and to handle global events.
 *
 * @author Foxikle
 */
public class EventHandler {
    private final GlobalEventHandler GLOBAL_HANDLER;
    private final Map<String, EventListener<? extends Event>> NAMESPACED_HANDLERS = new HashMap<>();
    private boolean initialized = false;

    /**
     * Constructor for EventHandler.
     * Initializes the GlobalEventHandler instance.
     *
     * @param globalHandler The GlobalEventHandler instance to be used.
     */
    public EventHandler(GlobalEventHandler globalHandler) {
        GLOBAL_HANDLER = globalHandler;
    }

    public void init() {
        if (initialized) throw new IllegalStateException("The event handler has already been initialized!");
        setupInternalListeners();
        initialized = true;
    }

    /**
     * Unregisters a listener by its namespace.
     *
     * @param listener The listener to be unregistered.
     * @return True if the listener was successfully unregistered, false otherwise.
     */
    public boolean unregisterListener(EventListener<? extends Event> listener) {
        return NAMESPACED_HANDLERS.remove(listener.getNamespace()) != null;
    }

    /**
     * Unregisters a listener by its namespace.
     *
     * @param namespace The namespace of the listener to be unregistered.
     * @return True if the listener was successfully unregistered, false otherwise.
     */
    public boolean unregisterListener(String namespace) {
        return NAMESPACED_HANDLERS.remove(namespace) != null;
    }

    /**
     * Registers a listener.
     *
     * @param listener The listener to be registered.
     * @return True if the listener was successfully registered, false otherwise.
     */
    public boolean registerListener(EventListener<? extends Event> listener) {
        return NAMESPACED_HANDLERS.putIfAbsent(listener.getNamespace(), listener) == listener;
    }

    public <T extends Event> void handleEvent(T event) {
        List<EventListener<? extends Event>> matchingListeners = new ArrayList<>();
        for (EventListener<? extends Event> listener : NAMESPACED_HANDLERS.values()) {
            if (listener.getEventClass() == event.getClass() &&
                    !(event instanceof CancellableEvent && ((CancellableEvent) event).isCancelled())) {
                matchingListeners.add(listener);
            }
        }
        // Sort listeners by priority
        matchingListeners.sort(Comparator.comparingInt(EventListener::getPriority));

        for (EventListener<? extends Event> listener : matchingListeners) {
            if (!(event instanceof CancellableEvent && ((CancellableEvent) event).isCancelled()))
                listener.complete(event);
        }
    }

    private void setupInternalListeners() {
        //book events
        GLOBAL_HANDLER.addListener(EditBookEvent.class, (this::handleEvent));
        // entity events
        GLOBAL_HANDLER.addListener(EntityAttackEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(EntityDamageEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(EntityDeathEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(EntityDespawnEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(EntityFireEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(EntityItemMergeEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(EntityPotionAddEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(EntityPotionRemoveEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(EntityShootEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(EntitySpawnEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(EntityTickEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(EntityVelocityEvent.class, (this::handleEvent));
        //projectile events
        GLOBAL_HANDLER.addListener(ProjectileCollideWithBlockEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(ProjectileCollideWithEntityEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(ProjectileUncollideEvent.class, (this::handleEvent));
        // Instance events
        GLOBAL_HANDLER.addListener(AddEntityToInstanceEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(InstanceChunkLoadEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(InstanceChunkUnloadEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(InstanceRegisterEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(InstanceTickEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(InstanceUnregisterEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(RemoveEntityFromInstanceEvent.class, (this::handleEvent));
        // Inventory Events
        GLOBAL_HANDLER.addListener(InventoryClickEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(InventoryCloseEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(InventoryItemChangeEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(InventoryOpenEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(InventoryPreClickEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerInventoryItemChangeEvent.class, (this::handleEvent));
        // Item Events
        GLOBAL_HANDLER.addListener(EntityEquipEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(ItemDropEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(ItemUpdateStateEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PickupExperienceEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PickupItemEvent.class, (this::handleEvent));
        // player events
        GLOBAL_HANDLER.addListener(AdvancementTabEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(AsyncPlayerConfigurationEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(AsyncPlayerPreLoginEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerBlockBreakEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerBlockInteractEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerBlockPlaceEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerCancelDiggingEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerChangeHeldSlotEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerChatEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerChunkLoadEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerChunkUnloadEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerCommandEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerDeathEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerDisconnectEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerEatEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerEntityInteractEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerFinishDiggingEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerGameModeChangeEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerHandAnimationEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerItemAnimationEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerMoveEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerPacketEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerPacketOutEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerPluginMessageEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerPreEatEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerResourcePackStatusEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerRespawnEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerSettingsChangeEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerSkinInitEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerSpawnEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerSpectateEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerStartDiggingEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerStartFlyingEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerStartFlyingWithElytraEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerStartSprintingEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerStartSneakingEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerStopFlyingEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerStopFlyingWithElytraEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerStopSprintingEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerStopSneakingEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerSwapItemEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerTickEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerUseItemEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(PlayerUseItemOnBlockEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(UpdateTagListEvent.class, (this::handleEvent)); // deprecated
        // Server
        GLOBAL_HANDLER.addListener(ClientPingServerEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(ServerListPingEvent.class, (this::handleEvent));
        GLOBAL_HANDLER.addListener(ServerTickMonitorEvent.class, (this::handleEvent));
    }
}