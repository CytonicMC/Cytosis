package net.cytonic.cytosis.protocol.listeners;

import net.minestom.server.event.EventDispatcher;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.events.network.PlayerJoinNetworkEvent;
import net.cytonic.cytosis.events.network.PlayerLeaveNetworkEvent;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.protocol.impl.notify.PlayerLoginLogoutNotifyPacket;
import net.cytonic.protocol.utils.NotifyHandler;

public class PlayerLoginLogoutNotifyListener {

    private static final CytonicNetwork network = Cytosis.get(CytonicNetwork.class);
    private static final PreferenceManager preferenceManager = Cytosis.get(PreferenceManager.class);
    private static final FriendManager friendManager = Cytosis.get(FriendManager.class);
    private static final RankManager rankManager = Cytosis.get(RankManager.class);

    @NotifyHandler(subject = Subjects.PLAYER_JOIN)
    public static void onJoin(PlayerLoginLogoutNotifyPacket.Packet packet) {
        EventDispatcher.call(new PlayerJoinNetworkEvent(packet.uuid(), packet.username()));
        network.addPlayer(packet.username(), packet.uuid());
        preferenceManager.loadPlayerPreferences(packet.uuid());
        friendManager.sendLoginMessage(packet.uuid());
        rankManager.loadPlayer(packet.uuid());
    }

    @NotifyHandler(subject = Subjects.PLAYER_LEAVE)
    public static void onLeave(PlayerLoginLogoutNotifyPacket.Packet packet) {
        EventDispatcher.call(new PlayerLeaveNetworkEvent(packet.uuid(), packet.username()));
        network.removePlayer(packet.username(), packet.uuid());
        preferenceManager.unloadPlayerPreferences(packet.uuid());
        friendManager.sendLogoutMessage(packet.uuid());
    }
}
