package net.cytonic.cytosis.managers;

import lombok.Getter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.containers.snooper.*;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SnooperManager {
    private final Map<SnooperRecieveEvent, Predicate<SnooperRecieveEvent>> events = new ConcurrentHashMap<>();
    @Getter
    private final SnoopPersistenceManager persistenceManager;
    private final SnooperRegistry registry = new SnooperRegistry();
    private Set<String> stored = new HashSet<>();

    public SnooperManager() {
        persistenceManager = new SnoopPersistenceManager(Cytosis.getDatabaseManager().getMysqlDatabase());
    }

    public void loadChannelsFromRedis() {
        stored = Cytosis.getDatabaseManager().getRedisDatabase().getSet("cytosis:snooper_channels");
        for (String channel : stored) {
            try {
                registerChannel(SnooperChannel.deserialize(channel));
            } catch (Exception e) {
                Logger.error("error: ", e);
            }
        }
    }

    public void registerChannel(SnooperChannel channel) {

        if (registry.getChannel(channel.id()) != null) {
            // prevents duplicating messages
            return;
        }

        registry.registerChannel(channel);
        if (!stored.contains(channel.serialize())) {
            // we should put it in redis!
            Cytosis.getDatabaseManager().getRedisDatabase().addValue("cytosis:snooper_channels", channel.serialize());
            stored.add(channel.serialize());
        }

        Cytosis.getNatsManager().subscribe(channel.channel(), message -> {
            SnooperContainer container = SnooperContainer.deserialize(message.getData());

            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
                if (!player.isStaff()) return;
                if (!player.canRecieveSnoop(channel.recipients())) return;
                if (player.getPreference(CytosisNamespaces.MUTE_SNOOPER)) return;
                if (!player.getPreference(CytosisNamespaces.LISTENING_SNOOPS).snoops().contains(channel.id().asString()))
                    return;


                player.sendMessage(container.message());
            }

            // montior the snoops on this channel
            events.forEach((event, pred) -> {
                if (pred.test(event)) {
                    event.onReceive(channel, container);
                }
            });

        });
    }

    /**
     * A mirror of {@link SnooperManager#onSnoop(SnooperRecieveEvent, Predicate)} with an unrestrictive predicate.
     *
     * @param event The reception event, used to monitor.
     */
    public void onSnoop(SnooperRecieveEvent event) {
        onSnoop(event, ignored -> true);
    }

    /**
     * Registers an external listener for a snoop. These have no effect on the delivery on snoops,
     * as these listeners are called after sending the messages.
     *
     * @param event     The reception event
     * @param predicate The predicate used to filter the messsages
     */
    public void onSnoop(SnooperRecieveEvent event, Predicate<SnooperRecieveEvent> predicate) {
        events.put(event, predicate);
    }

    /**
     * Sends a COMPELTED message to a channel. This means the component supplied is sent as is.
     * <strong>DO NOT LOG THESE MESSAGES</strong>, the snooper API already logs any message sent.
     * (The formatting is stripped, to save space.)
     *
     * @param channel The registered channel to send the message on.
     * @param message The message to send.
     */
    public void sendSnoop(SnooperChannel channel, Component message) {
        persistenceManager.persistSnoop(channel, message).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                Logger.error("error persisting snoop!: ", throwable);
            }
        });
        Cytosis.getNatsManager().publish(channel.channel(), SnooperContainer.pipeline(message));
    }

    public void snoop(CytosisPlayer player, @NotNull String channel) {
        SnoopsContainer container = player.getPreference(CytosisNamespaces.LISTENING_SNOOPS);
        if (container.snoops().contains(channel)) {
            player.sendMessage(Msg.whoops(" You are already snooping on the channel '" + channel + "'"));
            return;
        }
        player.updatePreference(CytosisNamespaces.LISTENING_SNOOPS, container.with(channel));
        player.sendMessage(Msg.mm("<b><#e829aa>SNOOPED!</#e829aa></b><gray> Successfully started snooping on the '" + channel + "' channel!"));
    }

    public void blind(CytosisPlayer player, @NotNull String channel) {
        SnoopsContainer container = player.getPreference(CytosisNamespaces.LISTENING_SNOOPS);
        if (!container.snoops().contains(channel)) {
            player.sendMessage(Msg.whoops("You are not snooping on the channel '" + channel + "'"));
            return;
        }
        player.updatePreference(CytosisNamespaces.LISTENING_SNOOPS, container.without(channel));
        player.sendMessage(Msg.mm("<b><#ff0034>DESNOOPED!</#ff0034></b><gray> Successfully stopped snooping on the '" + channel + "' channel!"));
    }

    /**
     * Gets the availble channels the player has access to
     *
     * @param player The player
     * @return The set of channels
     */
    public Set<String> getAllChannels(CytosisPlayer player) {
        return registry.channels.values().stream().filter(c -> player.canRecieveSnoop(c.recipients())).map(channel -> channel.id().asString()).collect(Collectors.toSet());
    }

    @Nullable
    public SnooperChannel getChannel(NamespaceID namespaceID) {
        return registry.getChannel(namespaceID);
    }

    private class SnooperRegistry {
        private final Map<NamespaceID, SnooperChannel> channels = new ConcurrentHashMap<>();

        protected SnooperRegistry() {
        }

        @Nullable
        public SnooperChannel getChannel(NamespaceID namespaceID) {
            return channels.get(namespaceID);
        }

        protected void registerChannel(SnooperChannel channel) {
            if (channels.containsKey(channel.id()))
                throw new IllegalArgumentException("Already registered channel " + channel.id().asString());
            channels.put(channel.id(), channel);
        }
    }
}
