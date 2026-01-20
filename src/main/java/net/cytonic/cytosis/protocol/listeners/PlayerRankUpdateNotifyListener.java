package net.cytonic.cytosis.protocol.listeners;

import com.google.errorprone.annotations.Keep;
import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.NotifyData;
import net.cytonic.protocol.impl.notifyPackets.PlayerRankUpdateNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyListener;

@Keep
public class PlayerRankUpdateNotifyListener implements NotifyListener<Packet> {

    @Override
    public void onMessage(Packet message, NotifyData notifyData) {
        RankManager rankManager = Cytosis.get(RankManager.class);
        PlayerRank rank = PlayerRank.valueOf(message.rank());
        Cytosis.getPlayer(message.player()).ifPresentOrElse(player -> {
            // they are on this server, so we need to update their cosmetics
            rankManager.changeRank(player, rank);
            Component badge;
            if (rank != PlayerRank.DEFAULT) {
                badge = rank.getPrefix().replaceText(builder -> builder.match(" ").replacement(""));
            } else {
                badge = Component.text(PlayerRank.DEFAULT.name(), PlayerRank.DEFAULT.getTeamColor());
            }
            player.sendMessage(Msg.network("Your rank has been updated to ").append(badge).append(Msg.grey(".")));

        }, () -> {
            rankManager.changeRankSilently(message.player(), rank);
            Cytosis.get(CytonicNetwork.class).updateCachedPlayerRank(message.player(), rank);
        });
    }
}
