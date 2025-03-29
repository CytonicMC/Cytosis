package net.cytonic.cytosis.managers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
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
    public void enableVanish(Player player) {
        vanished.put(player.getUuid(), player.getEntityId());
        EntityMetaDataPacket invis = new EntityMetaDataPacket(player.getEntityId(), Map.of(0, Metadata.Byte((byte) (0x20 | 0x40))));
        TeamsPacket selfTeam = new TeamsPacket("vanished", new TeamsPacket.CreateTeamAction(Msg.mm(""),
                (byte) 0x02, TeamsPacket.NameTagVisibility.HIDE_FOR_OTHER_TEAMS, TeamsPacket.CollisionRule.NEVER,
                NamedTextColor.GRAY, Msg.coloredBadge("VANISHED!", "gray"), Msg.mm(""), List.of(player.getUsername())));
        player.sendPackets(invis, selfTeam);
        player.updateViewableRule(p -> {
            CytosisPlayer cp = (CytosisPlayer) p;
            if (cp.isStaff()) {
                TeamsPacket packet = new TeamsPacket("vanished", new TeamsPacket.CreateTeamAction(Msg.mm(""),
                        (byte) 0x02, TeamsPacket.NameTagVisibility.HIDE_FOR_OTHER_TEAMS, TeamsPacket.CollisionRule.NEVER,
                        NamedTextColor.GRAY, Msg.coloredBadge("VANISHED!", "gray"), Msg.mm(""), List.of(p.getUsername(), player.getUsername())));
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
        vanished.remove(player.getUuid());

        Map<Integer, Metadata.Entry<?>> entries = new HashMap<>(player.getMetadataPacket().entries());
        byte byteVal = 0;
        if (entries.containsKey(0)) {
            byteVal = (byte) entries.get(0).value();
        }
        byteVal &= ~(0x20 | 0x40);
        entries.put(0, Metadata.Byte(byteVal));

        Cytosis.getRankManager().setupCosmetics(player, Cytosis.getCytonicNetwork().getPlayerRanks().get(player.getUuid()));
        PacketSendingUtils.broadcastPlayPacket(new EntityMetaDataPacket(player.getEntityId(), entries));
        player.updateViewableRule(p -> true);
    }

    public boolean isVanished(UUID uuid) {
        return vanished.containsKey(uuid);
    }
}
