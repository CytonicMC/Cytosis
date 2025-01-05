package net.cytonic.cytosis.managers;

import lombok.Getter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;

import java.util.*;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * This class handles vanishing
 */
public class VanishManager {

    private final List<UUID> vanishedPlayers = new ArrayList<>();
    @Getter
    private final List<Integer> vanishedEntityIds = new ArrayList<>();

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
    public void enableVanish(Player player) {
        vanishedPlayers.add(player.getUuid());
        vanishedEntityIds.add(player.getEntityId());
        EntityMetaDataPacket invis = new EntityMetaDataPacket(player.getEntityId(), Map.of(0, Metadata.Byte((byte) (0x20 | 0x40))));
        TeamsPacket selfTeam = new TeamsPacket("vanished", new TeamsPacket.CreateTeamAction(MM."",
                (byte) 0x02, TeamsPacket.NameTagVisibility.HIDE_FOR_OTHER_TEAMS, TeamsPacket.CollisionRule.NEVER,
                NamedTextColor.GRAY, MM."<gray><b>VANISHED! ", MM."", List.of(player.getUsername())));
        player.sendPackets(invis, selfTeam);
        player.updateViewableRule(p -> {
            CytosisPlayer cp = (CytosisPlayer) p;
            if (cp.isStaff()) {
                TeamsPacket packet = new TeamsPacket("vanished", new TeamsPacket.CreateTeamAction(MM."",
                        (byte) 0x02, TeamsPacket.NameTagVisibility.HIDE_FOR_OTHER_TEAMS, TeamsPacket.CollisionRule.NEVER,
                        NamedTextColor.GRAY, MM."<gray><b>VANISHED! ", MM."", List.of(p.getUsername(), player.getUsername())));
                p.sendPackets(packet, invis);
                return true;
            }
            return false;
        });
        //todo events?
    }

    /**
     * Disables vanish for a player
     *
     * @param player the player to unvanish
     */
    public void disableVanish(CytosisPlayer player) {
        vanishedPlayers.remove(player.getUuid());
        vanishedEntityIds.remove((Object) player.getEntityId());

        Map<Integer, Metadata.Entry<?>> entries = new HashMap<>(player.getMetadataPacket().entries());
        byte byteVal = 0;
        if (entries.containsKey(0)) {
            byteVal = (byte) entries.get(0).value();
        }
        byteVal &= ~(0x20 | 0x40);
        entries.put(0, Metadata.Byte(byteVal));
        var packet = new EntityMetaDataPacket(player.getEntityId(), entries);

        Cytosis.getRankManager().setupCosmetics(player, Cytosis.getCytonicNetwork().getPlayerRanks().get(player.getUuid()));
        player.sendPacket(packet);
        Cytosis.getOnlinePlayers().forEach(p -> p.sendPacket(packet));
        player.updateViewableRule(_ -> true);
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }
}
