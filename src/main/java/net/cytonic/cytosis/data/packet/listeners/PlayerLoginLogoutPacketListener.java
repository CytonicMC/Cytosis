package net.cytonic.cytosis.data.packet.listeners;

import lombok.NoArgsConstructor;
import net.minestom.server.event.EventDispatcher;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.packet.packets.PlayerLoginLogoutPacket;
import net.cytonic.cytosis.data.packet.utils.PacketHandler;
import net.cytonic.cytosis.events.network.PlayerJoinNetworkEvent;
import net.cytonic.cytosis.events.network.PlayerLeaveNetworkEvent;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.cytosis.messaging.Subjects;

@CytosisComponent(dependsOn = {CytonicNetwork.class, PreferenceManager.class, FriendManager.class, RankManager.class})
@NoArgsConstructor
public class PlayerLoginLogoutPacketListener implements Bootstrappable {

    private CytonicNetwork network;
    private PreferenceManager preferenceManager;
    private FriendManager friendManager;
    private RankManager rankManager;

    @Override
    public void init() {
        this.network = Cytosis.get(CytonicNetwork.class);
        this.preferenceManager = Cytosis.get(PreferenceManager.class);
        this.friendManager = Cytosis.get(FriendManager.class);
        this.rankManager = Cytosis.get(RankManager.class);
    }

    @PacketHandler(subject = Subjects.PLAYER_JOIN)
    private void handlePlayerJoin(PlayerLoginLogoutPacket packet) {
        EventDispatcher.call(new PlayerJoinNetworkEvent(packet.getUuid(), packet.getUsername()));
        network.addPlayer(packet.getUsername(), packet.getUuid());
        preferenceManager.loadPlayerPreferences(packet.getUuid());
        friendManager.sendLoginMessage(packet.getUuid());
        rankManager.loadPlayer(packet.getUuid());
    }

    @PacketHandler(subject = Subjects.PLAYER_LEAVE)
    private void handlePlayerLeave(PlayerLoginLogoutPacket packet) {
        EventDispatcher.call(new PlayerLeaveNetworkEvent(packet.getUuid(), packet.getUsername()));
        network.removePlayer(packet.getUsername(), packet.getUuid());
        preferenceManager.unloadPlayerPreferences(packet.getUuid());
        friendManager.sendLogoutMessage(packet.getUuid());
        rankManager.removePlayer(packet.getUuid());
    }
}
