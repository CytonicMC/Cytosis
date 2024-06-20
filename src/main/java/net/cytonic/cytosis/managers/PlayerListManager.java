package net.cytonic.cytosis.managers;

import lombok.Getter;
import lombok.Setter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.playerlist.PlayerListCategory;
import net.cytonic.cytosis.playerlist.PlayerListEntry;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.utils.PacketUtils;

import java.util.*;

/**
 * A class that manages the player list
 */
@Setter
@Getter
public class PlayerListManager {

    /**
     * The default player list manager constructor
     */
    public PlayerListManager() {
        // do nothing
    }

    //todo make it per player?
    private List<PlayerListCategory> globalCategories = new ArrayList<>();

    /**
     * Injects the player list packets into the player's connection
     *
     * @param player The player to set up
     */
    public void setupPlayer(Player player) {
        // remove them from the player list for everyone, but keep skin data
        PacketUtils.broadcastPlayPacket(new PlayerInfoUpdatePacket(
                PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                new PlayerInfoUpdatePacket.Entry(player.getUuid(), player.getUsername(), List.of(
                        new PlayerInfoUpdatePacket.Property("textures", player.getSkin().textures(), player.getSkin().signature())
                ), false, player.getLatency(), player.getGameMode(), player.getDisplayName(), null)
        ));

        // remove everyone from the player


        for (ServerPacket packet : createPackets()) {
            player.sendPacket(packet);
        }
    }

    /**
     * Creates the player list packets
     *
     * @return the list of packets
     */
    private List<ServerPacket> createPackets() {
        globalCategories.sort(Comparator.comparingInt(PlayerListCategory::getPriority));
        List<ServerPacket> packets = new ArrayList<>();

        char currentLetter = 'A';

        for (PlayerListCategory category : globalCategories) { // category naming is "A0",then the next category is B0, etc.
            category.sortEntries();

            int currentInt = 1;
            UUID categoryUUID = UUID.randomUUID();

            PlayerInfoUpdatePacket categoryPacket = new PlayerInfoUpdatePacket(
                    EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED, PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
                    List.of(new PlayerInfoUpdatePacket.Entry(categoryUUID, STR."\{currentLetter}0", category.getFavicon().property(), true, 1, GameMode.CREATIVE, category.getName(), null)));
            packets.add(categoryPacket);

            for (PlayerListEntry entry : category.getEntries()) {
                UUID entryUUID = UUID.randomUUID();
                PlayerInfoUpdatePacket entryPacket = new PlayerInfoUpdatePacket(
                        EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED, PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
                        List.of(new PlayerInfoUpdatePacket.Entry(entryUUID, STR."\{currentLetter}\{currentInt}",
                                List.of(entry.getFavicon().getProperty()), true, 1,
                                GameMode.CREATIVE, entry.getName(), null)));
                currentInt++;
                packets.add(entryPacket);
//                packets.add(espawn);
            }
            currentLetter++;
        }

        // remove online players too
        for (Player player : Cytosis.getOnlinePlayers()) {
            packets.add(new PlayerInfoUpdatePacket(
                    PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                    new PlayerInfoUpdatePacket.Entry(player.getUuid(), player.getUsername(), List.of(
                            new PlayerInfoUpdatePacket.Property("textures", player.getSkin().textures(), player.getSkin().signature())
                    ), false, player.getLatency(), player.getGameMode(), player.getDisplayName(), null)
            ));
        }

        return packets;
    }
}
