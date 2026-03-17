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

    @NotifyHandler(subject = Subjects.PLAYER_JOIN)
    public static void onJoin(PlayerLoginLogoutNotifyPacket.Packet packet) {
        EventDispatcher.call(new PlayerJoinNetworkEvent(packet.uuid(), packet.username()));
        Cytosis.get(CytonicNetwork.class).addPlayer(packet.username(), packet.uuid());
        Cytosis.get(PreferenceManager.class).loadPlayerPreferences(packet.uuid());
        Cytosis.get(FriendManager.class).sendLoginMessage(packet.uuid());
        Cytosis.get(RankManager.class).loadPlayer(packet.uuid());
    }

    @NotifyHandler(subject = Subjects.PLAYER_LEAVE)
    public static void onLeave(PlayerLoginLogoutNotifyPacket.Packet packet) {
        EventDispatcher.call(new PlayerLeaveNetworkEvent(packet.uuid(), packet.username()));
        Cytosis.get(CytonicNetwork.class).removePlayer(packet.username(), packet.uuid());
        Cytosis.get(PreferenceManager.class).unloadPlayerPreferences(packet.uuid());
        Cytosis.get(FriendManager.class).sendLogoutMessage(packet.uuid());
    }
}
