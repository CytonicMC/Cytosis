package net.cytonic.cytosis.managers;

import io.nats.client.Dispatcher;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.containers.snooper.SnooperChannel;
import net.cytonic.cytosis.data.containers.snooper.SnooperContainer;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SnooperManager {
    private Map<NamespaceID, Dispatcher> channels = new ConcurrentHashMap<>();


    public void registerChannel(SnooperChannel channel) {
        Cytosis.getNatsManager().subscribe(channel.channel(), message -> {
            SnooperContainer container = SnooperContainer.deserialize(message.getData());
            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) { //todo: add support for disabling this
                if (player.canRecieveSnoop(channel.recipients())) {
                    player.sendMessage(container.message());
                }
            }
        });
    }

    public void sendSnoop(SnooperChannel channel, Component message) {
        // todo: log snoops on send
        Cytosis.getNatsManager().publish(channel.channel(), SnooperContainer.pipeline(message));
    }


    private class SnooperRegistry {
        private final Map<NamespaceID, SnooperChannel> channels = new ConcurrentHashMap<>();

        protected SnooperRegistry() {
        }

        @Nullable
        protected SnooperChannel getChannel(NamespaceID namespaceID) {
            return channels.get(namespaceID);
        }

        protected void registerChannel(NamespaceID namespaceID, SnooperChannel channel) {
            if (channels.containsKey(namespaceID))
                throw new IllegalArgumentException("Already registered channel " + namespaceID.asString());
            channels.put(namespaceID, channel);
        }
    }
}
