package net.cytonic.cytosis.managers;

import net.cytonic.cytosis.Cytosis;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;

import java.util.List;
import java.util.Map;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * This class handles vanishing
 */
public class VanishManager {

    /**
     * A default constructor for VanishManager
     */
    public VanishManager() {

    }

    /**
     * Enables vanish for a player
     *
     * @param player the player to vanish
     */
    @SuppressWarnings("UnstableApiUsage")
    public void enableVanish(Player player) {
        player.updateViewableRule(p -> {
            if (p.hasPermission("cytosis.vanish.can_see_vanished")) {
                TeamsPacket packet = new TeamsPacket("vanished", new TeamsPacket.CreateTeamAction(MM."",
                        (byte) 0x02, TeamsPacket.NameTagVisibility.HIDE_FOR_OTHER_TEAMS, TeamsPacket.CollisionRule.NEVER,
                        NamedTextColor.GRAY, MM."<gray><b>VANISHED! ", MM."", List.of(p.getUsername(), player.getUsername())));
                EntityMetaDataPacket invis = new EntityMetaDataPacket(player.getEntityId(), Map.of(0, Metadata.Byte((byte) (0x20 | 0x40))));
                p.sendPackets(packet, invis);
                return true;
            }
            return false;
        });
        //todo events?
    }

    /**
     * Disables vanish a player
     *
     * @param player the player to unvanish
     */
    @SuppressWarnings("UnstableApiUsage")
    public void disableVanish(Player player) {
        player.updateViewableRule(p -> {
            EntityMetaDataPacket invis = new EntityMetaDataPacket(player.getEntityId(), Map.of(0, Metadata.Byte((byte) 0)));
            p.sendPacket(invis);
            Cytosis.getRankManager().setupCosmetics(player, Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow());
            return true;
        });
    }
}
