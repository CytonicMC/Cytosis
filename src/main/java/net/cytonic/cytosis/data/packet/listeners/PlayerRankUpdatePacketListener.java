package net.cytonic.cytosis.data.packet.listeners;

import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.packet.packets.PlayerRankUpdatePacket;
import net.cytonic.cytosis.data.packet.utils.PacketHandler;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.utils.Msg;

@CytosisComponent
@NoArgsConstructor
public class PlayerRankUpdatePacketListener {

    @PacketHandler(subject = Subjects.PLAYER_RANK_UPDATE)
    private void listenForPlayerRankUpdate(PlayerRankUpdatePacket packet) {
        RankManager rankManager = Cytosis.get(RankManager.class);
        Cytosis.getPlayer(packet.getPlayer()).ifPresentOrElse(player -> {
            // they are on this server, so we need to update their cosmetics
            rankManager.changeRank(player, packet.getRank());
            Component badge;
            if (packet.getRank() != PlayerRank.DEFAULT) {
                badge = packet.getRank().getPrefix().replaceText(builder -> builder.match(" ").replacement(""));
            } else {
                badge = Component.text(PlayerRank.DEFAULT.name(), PlayerRank.DEFAULT.getTeamColor());
            }
            player.sendMessage(Msg.network("Your rank has been updated to ").append(badge).append(Msg.grey(".")));

        }, () -> {
            rankManager.changeRankSilently(packet.getPlayer(), packet.getRank());
            Cytosis.get(CytonicNetwork.class).updateCachedPlayerRank(packet.getPlayer(), packet.getRank());
        });
    }
}
