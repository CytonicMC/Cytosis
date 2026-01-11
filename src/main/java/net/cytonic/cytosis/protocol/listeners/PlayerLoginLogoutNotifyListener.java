package net.cytonic.cytosis.protocol.listeners;

import com.google.errorprone.annotations.Keep;
import net.minestom.server.event.EventDispatcher;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.events.network.PlayerJoinNetworkEvent;
import net.cytonic.cytosis.events.network.PlayerLeaveNetworkEvent;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.protocol.NotifyData;
import net.cytonic.protocol.NotifyListener;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.notifyPackets.PlayerLoginLogoutNotifyPacket;
import net.cytonic.protocol.notifyPackets.PlayerLoginLogoutNotifyPacket.Packet;

public class PlayerLoginLogoutNotifyListener {

    private static final CytonicNetwork network = Cytosis.get(CytonicNetwork.class);
    private static final PreferenceManager preferenceManager = Cytosis.get(PreferenceManager.class);
    private static final FriendManager friendManager = Cytosis.get(FriendManager.class);
    private static final RankManager rankManager = Cytosis.get(RankManager.class);

    @Keep
    public static class PlayerLoginNotifyListener implements NotifyListener<Packet> {

        @Override
        public ProtocolObject<Packet, ?> getProtocolObject() {
            return new PlayerLoginLogoutNotifyPacket(true);
        }

        @Override
        public void onMessage(Packet message, NotifyData notifyData) {
            EventDispatcher.call(new PlayerJoinNetworkEvent(message.uuid(), message.username()));
            network.addPlayer(message.username(), message.uuid());
            preferenceManager.loadPlayerPreferences(message.uuid());
            friendManager.sendLoginMessage(message.uuid());
            rankManager.loadPlayer(message.uuid());
        }
    }

    @Keep
    public static class PlayerLogoutNotifyListener implements NotifyListener<Packet> {

        @Override
        public ProtocolObject<Packet, ?> getProtocolObject() {
            return new PlayerLoginLogoutNotifyPacket(false);
        }

        @Override
        public void onMessage(Packet message, NotifyData notifyData) {
            EventDispatcher.call(new PlayerLeaveNetworkEvent(message.uuid(), message.username()));
            network.removePlayer(message.username(), message.uuid());
            preferenceManager.unloadPlayerPreferences(message.uuid());
            friendManager.sendLogoutMessage(message.uuid());
            rankManager.removePlayer(message.uuid());
        }
    }
}
