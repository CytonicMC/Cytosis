package net.cytonic.cytosis.managers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.containers.SnoopsContainer;
import net.cytonic.cytosis.data.objects.JsonComponent;
import net.cytonic.cytosis.data.packet.packets.SnooperPacket;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.snooper.SnoopPersistenceManager;
import net.cytonic.cytosis.snooper.SnooperChannel;
import net.cytonic.cytosis.snooper.SnooperRecieveEvent;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.Msg;

@CytosisComponent(dependsOn = {MysqlDatabase.class, NatsManager.class})
public class SnooperManager implements Bootstrappable {

    private final Map<SnooperRecieveEvent, Predicate<SnooperRecieveEvent>> events = new ConcurrentHashMap<>();
    private final SnooperRegistry registry = new SnooperRegistry();
    @Getter
    private SnoopPersistenceManager persistenceManager;
    private Set<String> stored = new HashSet<>();

    private NatsManager natsManager;

    public SnooperManager() {
    }

    @Override
    public void init() {
        this.natsManager = Cytosis.get(NatsManager.class);
        this.persistenceManager = new SnoopPersistenceManager(Cytosis.get(MysqlDatabase.class));

        Logger.info("Loading snooper channels from redis");
        loadChannelsFromRedis();
        Logger.info("Loading Cytosis snoops");
        registerChannel(CytosisSnoops.PLAYER_BAN);
        registerChannel(CytosisSnoops.PLAYER_UNBAN);
        registerChannel(CytosisSnoops.PLAYER_KICK);
        registerChannel(CytosisSnoops.PLAYER_UNMUTE);
        registerChannel(CytosisSnoops.PLAYER_MUTE);
        registerChannel(CytosisSnoops.PLAYER_WARN);
        registerChannel(CytosisSnoops.SERVER_ERROR);
        registerChannel(CytosisSnoops.CHANGE_RANK);
        registerChannel(CytosisSnoops.PLAYER_NICKNAME);
        registerChannel(CytosisSnoops.PLAYER_SERVER_CHANGE);
        registerChannel(CytosisSnoops.PLAYER_WHITELIST);
    }

    public void loadChannelsFromRedis() {
        stored = Cytosis.get(RedisDatabase.class).getSet("cytosis:snooper_channels");
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
            Cytosis.get(RedisDatabase.class).addValue("cytosis:snooper_channels", channel.serialize());
            stored.add(channel.serialize());
        }

        natsManager.subscribe(channel.channel(), message -> {
            SnooperPacket packet = SnooperPacket.getSerializer(SnooperPacket.class)
                .deserialize(new String(message.getData()));

            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
                if (!player.isStaff()) {
                    continue;
                }
                if (!player.canReceiveSnoop(channel.recipients())) {
                    continue;
                }
                if (player.getPreference(CytosisNamespaces.MUTE_SNOOPER)) {
                    continue;
                }
                if (!player.getPreference(CytosisNamespaces.LISTENING_SNOOPS).snoops()
                    .contains(channel.id().asString())) {
                    continue;
                }

                player.sendMessage(packet.getMessage().getComponent());
            }

            // montior the snoops on this channel
            events.forEach((event, pred) -> {
                if (pred.test(event)) {
                    event.onReceive(channel, packet);
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
     * Registers an external listener for a snoop. These have no effect on the delivery on snoops, as these listeners
     * are called after sending the messages.
     *
     * @param event     The reception event
     * @param predicate The predicate used to filter the messsages
     */
    public void onSnoop(SnooperRecieveEvent event, Predicate<SnooperRecieveEvent> predicate) {
        events.put(event, predicate);
    }

    /**
     * Sends a COMPLETED message to a channel. This means the component supplied is sent as is.
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
        new SnooperPacket(new JsonComponent(message), channel.channel()).publish();
    }

    public void snoop(CytosisPlayer player, @NotNull String channel) {
        SnoopsContainer container = player.getPreference(CytosisNamespaces.LISTENING_SNOOPS);
        if (container.snoops().contains(channel)) {
            player.sendMessage(Msg.whoops(" You are already snooping on the channel '" + channel + "'"));
            return;
        }
        player.updatePreference(CytosisNamespaces.LISTENING_SNOOPS, container.with(channel));
        player.sendMessage(
            Msg.splash("SNOOPED!", "e829aa", "Successfully started snooping on the '" + channel + "' channel!"));
    }

    public void blind(CytosisPlayer player, @NotNull String channel) {
        SnoopsContainer container = player.getPreference(CytosisNamespaces.LISTENING_SNOOPS);
        if (!container.snoops().contains(channel)) {
            player.sendMessage(Msg.whoops("You are not snooping on the channel '" + channel + "'"));
            return;
        }
        player.updatePreference(CytosisNamespaces.LISTENING_SNOOPS, container.without(channel));
        player.sendMessage(
            Msg.splash("DESNOOPED!", "ff0034", "Successfully stopped snooping on the '" + channel + "' channel!"));
    }

    /**
     * Gets the availble channels the player has access to
     *
     * @param player The player
     * @return The set of channels
     */
    public Set<String> getAllChannels(CytosisPlayer player) {
        return registry.channels.values().stream().filter(c -> player.canReceiveSnoop(c.recipients()))
            .map(channel -> channel.id().asString()).collect(Collectors.toSet());
    }

    @Nullable
    public SnooperChannel getChannel(Key namespaceID) {
        return registry.getChannel(namespaceID);
    }

    private static class SnooperRegistry {

        private final Map<Key, SnooperChannel> channels = new ConcurrentHashMap<>();

        protected SnooperRegistry() {
        }

        @Nullable
        public SnooperChannel getChannel(Key namespaceID) {
            return channels.get(namespaceID);
        }

        protected void registerChannel(SnooperChannel channel) {
            if (channels.containsKey(channel.id())) {
                throw new IllegalArgumentException("Already registered channel " + channel.id().asString());
            }
            channels.put(channel.id(), channel);
        }
    }
}