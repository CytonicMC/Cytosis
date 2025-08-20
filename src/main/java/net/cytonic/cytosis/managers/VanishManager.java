package net.cytonic.cytosis.managers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.events.VanishToggleEvent;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.MetadataPacketBuilder;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.utils.PacketSendingUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class handles vanishing
 */
@NoArgsConstructor
public class VanishManager {

    @Getter
    private final Map<UUID, Integer> vanished = new HashMap<>();

    /**
     * Enables vanish for a player
     *
     * @param player the player to vanish
     */
    public void enableVanish(CytosisPlayer player) {
        vanished.put(player.getUuid(), player.getEntityId());
        EntityMetaDataPacket metaPacket = MetadataPacketBuilder.builder(player.getMetadataPacket())
                .setGlowing(true)
                .setInvisible(true)
                .build();
        TeamsPacket selfTeam = new TeamsPacket("vanished", new TeamsPacket.CreateTeamAction(Msg.mm(""),
                (byte) 0x02, TeamsPacket.NameTagVisibility.HIDE_FOR_OTHER_TEAMS, TeamsPacket.CollisionRule.NEVER,
                NamedTextColor.GRAY, Msg.coloredBadge("VANISHED! ", "gray"), Msg.mm(""), List.of(player.getUsername())));
        player.sendPackets(metaPacket, selfTeam);
        player.updateViewableRule(p -> {
            CytosisPlayer cp = (CytosisPlayer) p;
            if (cp.isStaff()) {
                TeamsPacket packet = new TeamsPacket("vanished", new TeamsPacket.CreateTeamAction(Msg.mm(""),
                        (byte) 0x02, TeamsPacket.NameTagVisibility.HIDE_FOR_OTHER_TEAMS, TeamsPacket.CollisionRule.NEVER,
                        NamedTextColor.GRAY, Msg.coloredBadge("VANISHED! ", "gray"), Msg.mm(""), List.of(p.getUsername(), player.getUsername())));
                p.sendPackets(packet, metaPacket);
                return true;
            }
            return false;
        });
        EventDispatcher.call(new VanishToggleEvent(true, player));
    }

    /**
     * Disables vanish for a player
     *
     * @param player the player to unvanish
     */
    public void disableVanish(CytosisPlayer player) {
        vanished.remove(player.getUuid());

        Cytosis.getRankManager().setupCosmetics(player, player.getRank());
        PacketSendingUtils.broadcastPlayPacket(player.getMetadataPacket());
        player.updateViewableRule(p -> true);
        EventDispatcher.call(new VanishToggleEvent(false, player));
    }

    public boolean isVanished(UUID uuid) {
        return vanished.containsKey(uuid);
    }
}
