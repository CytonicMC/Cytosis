package net.cytonic.cytosis.managers;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * This class handles vanishing
 */
public class VanishManager {

    private final List<UUID> vanishedPlayers = new ArrayList<>();

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
        vanishedPlayers.add(player.getUuid());
        player.updateViewableRule(p -> {
            CytosisPlayer cp = (CytosisPlayer) p;
            List<String> perms = List.of(cp.getRank().getPermissions());
            if (perms.contains("cytosis.vanish.can_see_vanished")) {
                TeamsPacket packet = new TeamsPacket("vanished", new TeamsPacket.CreateTeamAction(MM."",
                        (byte) 0x02, TeamsPacket.NameTagVisibility.HIDE_FOR_OTHER_TEAMS, TeamsPacket.CollisionRule.NEVER,
                        NamedTextColor.GRAY, MM."<gray><b>VANISHED! ", MM."", List.of(p.getUsername(), player.getUsername())));
                EntityMetaDataPacket invis = new EntityMetaDataPacket(player.getEntityId(), Map.of(0, Metadata.Byte((byte) (0x20 | 0x40))));
                p.sendPackets(packet, invis);
                Logger.debug(STR."Hey btw um \{player.getUsername()} should be visible to \{p.getUsername()}, but liked vanished");
                p.sendMessage(STR."Hey btw um \{player.getUsername()} should be visible, but liked vanished");
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
        vanishedPlayers.remove(player.getUuid());
        player.updateViewableRule(p -> {
            EntityMetaDataPacket invis = new EntityMetaDataPacket(player.getEntityId(), Map.of(0, Metadata.Byte((byte) 0)));
            p.sendPacket(invis);
            Cytosis.getRankManager().setupCosmetics(player, Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow());
            return true;
        });
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }
}
