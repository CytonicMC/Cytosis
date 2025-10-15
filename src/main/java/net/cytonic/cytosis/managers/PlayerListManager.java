package net.cytonic.cytosis.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket.Property;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.PacketSendingUtils;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.playerlist.Column;
import net.cytonic.cytosis.playerlist.DefaultPlayerListCreator;
import net.cytonic.cytosis.playerlist.PlayerListEntry;
import net.cytonic.cytosis.playerlist.PlayerListFavicon;
import net.cytonic.cytosis.playerlist.PlayerlistCreator;

/**
 * A class that manages the player list
 */
@Setter
@Getter
public class PlayerListManager implements Bootstrappable {

    private final Map<UUID, Component[][]> playerComponents = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerInfoUpdatePacket.Property[][]> playerFavicons = new ConcurrentHashMap<>();
    private UUID[][] listUuids; // <column, entry>
    private PlayerlistCreator creator;
    // in ticks
    private int updateInterval = 20;

    /**
     * The default player list manager constructor
     */
    public PlayerListManager() {

    }

    @Override
    public void init() {
        creator = new DefaultPlayerListCreator();
        scheduleUpdate();
        listUuids = new UUID[creator.getColumnCount()][20];
        for (int i = 0; i < listUuids.length; i++) {
            for (int j = 0; j < listUuids[i].length; j++) {
                listUuids[i][j] = UUID.randomUUID();
            }
        }
    }

    public void setCreator(PlayerlistCreator creator) {
        this.creator = creator;

        listUuids = new UUID[creator.getColumnCount()][20];
        for (int i = 0; i < listUuids.length; i++) {
            for (int j = 0; j < listUuids[i].length; j++) {
                listUuids[i][j] = UUID.randomUUID();
            }
        }

        playerComponents.clear();
        playerFavicons.clear();
    }

    /**
     * Injects the player list packets into the player's connection
     *
     * @param player The player to set up
     */
    public void setupPlayer(CytosisPlayer player) {
        player.sendPlayerListHeaderAndFooter(creator.header(player), creator.footer(player));
        // remove them from the player list for everyone, but keep skin data
        PacketSendingUtils.broadcastPlayPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
            new PlayerInfoUpdatePacket.Entry(player.getUuid(), player.getUsername(), List.of(
                new PlayerInfoUpdatePacket.Property("textures", player.getSkin().textures(),
                    player.getSkin().signature())), false, player.getLatency(), player.getGameMode(),
                player.getDisplayName(), null, -1, true)));

        player.sendPackets(createInjectPackets(player));
    }

    /**
     * Creates the player list packets
     *
     * @return the list of packets
     */
    private List<SendablePacket> createInjectPackets(Player player) {
        List<SendablePacket> packets = new ArrayList<>();
        if (!playerComponents.containsKey(player.getUuid())) {
            Component[][] components = new Component[creator.getColumnCount()][20];
            for (int i = 0; i < components.length; i++) {
                for (int j = 0; j < components[i].length; j++) {
                    components[i][j] = Component.empty();
                }
            }
            playerComponents.put(player.getUuid(), components);
        }

        if (!playerFavicons.containsKey(player.getUuid())) {
            PlayerInfoUpdatePacket.Property[][] prp = new PlayerInfoUpdatePacket.Property[creator.getColumnCount()][20];
            for (PlayerInfoUpdatePacket.Property[] favicon : prp) {
                Arrays.fill(favicon, PlayerListFavicon.GREY.getProperty());
            }
            playerFavicons.put(player.getUuid(), prp);
        }

        int order = 0;
        for (int i = 0; i < listUuids.length; i++) {
            char col = (char) ('A' + i);
            for (int j = 0; j < listUuids[i].length; j++) {
                char row = (char) ('a' + j);
                UUID uuid = listUuids[i][j];
                packets.add(new PlayerInfoUpdatePacket(
                    EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                        PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME), List.of(
                    new PlayerInfoUpdatePacket.Entry(uuid, "!" + col + "-" + row,
                        List.of(playerFavicons.get(player.getUuid())[i][j]), true, 1, GameMode.CREATIVE,
                        playerComponents.get(player.getUuid())[i][j], null, order, true))));
                order++;
            }
        }

        // remove online players too
        for (Player p : Cytosis.getOnlinePlayers()) {
            packets.add(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                new PlayerInfoUpdatePacket.Entry(p.getUuid(), p.getUsername(), List.of(
                    new PlayerInfoUpdatePacket.Property("textures", p.getSkin().textures(), p.getSkin().signature())),
                    false, p.getLatency(), p.getGameMode(), p.getDisplayName(), null, -1, true)));
        }
        return packets;
    }

    private void createComponentsForPlayer(CytosisPlayer player) {

    }

    /**
     * Update a player's player list
     *
     * @param player the player to update
     */
    public void update(CytosisPlayer player) {
        List<Column> columns = creator.createColumns(player);
        if (columns.size() != creator.getColumnCount()) {
            throw new IllegalArgumentException("Column count does not match size of column list");
        }

        columns.forEach(Column::sortEntries);
        PlayerInfoUpdatePacket.Property[][] updatedFavicons = toFavicons(new ArrayList<>(columns));
        Component[][] updatedComponents = toComponents(columns);

        player.sendPackets(createUpdatePackets(player.getUuid(), updatedComponents, updatedFavicons, columns));
    }

    /**
     * Updates all the players tab menus
     */
    public void updateAll() {
        Cytosis.getOnlinePlayers().forEach(this::update);
    }

    /**
     * Schedules the next update
     */
    private void scheduleUpdate() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            updateAll();
            scheduleUpdate();
        }).delay(TaskSchedule.tick(updateInterval)).schedule();
    }

    private List<SendablePacket> createUpdatePackets(UUID player, Component[][] updatedComponents,
        Property[][] updatedFavicons,
        List<Column> columns) {
        List<SendablePacket> updatePackets = new ArrayList<>();
        PlayerInfoUpdatePacket.Property[][] favicons = playerFavicons.getOrDefault(player, updatedFavicons);
        Component[][] components = playerComponents.getOrDefault(player, updatedComponents);
        int order = 0;
        for (int i = 0; i < updatedComponents.length; i++) {
            if (faviconNotEquals(favicons[i][0], updatedFavicons[i][0])) {
                updatePackets.add(new PlayerInfoRemovePacket(listUuids[i][0]));
                updatePackets.add(new PlayerInfoUpdatePacket(
                    EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                        PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
                    List.of(new PlayerInfoUpdatePacket.Entry(listUuids[i][0], "!" + (char) ('A' + i) + "-" + 'a',
                        List.of(updatedFavicons[i][0]), true, 1, GameMode.CREATIVE, updatedComponents[i][0], null,
                        order, true)
                    )));
            }

            if (!components[i][0].equals(columns.get(i).getName())) {
                updatePackets.add(new PlayerInfoUpdatePacket(
                    PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                    new PlayerInfoUpdatePacket.Entry(listUuids[i][0], "!" + (char) ('A' + i) + "-" + 'a',
                        List.of(updatedFavicons[i][0]), true, 1, GameMode.CREATIVE, updatedComponents[i][0], null,
                        order, true)
                ));
            }
            for (int j = 1; j < updatedComponents[i].length; j++) {
                if (faviconNotEquals(favicons[i][j], updatedFavicons[i][j])) {
                    updatePackets.add(new PlayerInfoRemovePacket(listUuids[i][j]));
                    updatePackets.add(new PlayerInfoUpdatePacket(
                        EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                            PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                            PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
                        List.of(new PlayerInfoUpdatePacket.Entry(listUuids[i][j],
                            "!" + (char) ('A' + i) + "-" + (char) ('a' + j),
                            List.of(updatedFavicons[i][j]), true, 1, GameMode.CREATIVE, updatedComponents[i][j], null,
                            order, true)
                        )));
                }
                if (!components[i][j].equals(updatedComponents[i][j])) {
                    components[i][j] = updatedComponents[i][j];
                    updatePackets.add(new PlayerInfoUpdatePacket(
                        PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                        new PlayerInfoUpdatePacket.Entry(listUuids[i][j],
                            "!" + (char) ('A' + i) + "-" + (char) ('a' + j),
                            List.of(updatedFavicons[i][j]), true, 1, GameMode.CREATIVE, updatedComponents[i][j], null,
                            order, true)
                    ));
                }
            }
        }
        playerFavicons.put(player, updatedFavicons);
        playerComponents.put(player, updatedComponents);
        return updatePackets;
    }

    /**
     * Converts a list of columns to the components arrays
     *
     * @param columns the columns to convert
     * @return an array with the components
     */
    private Component[][] toComponents(List<Column> columns) {
        Component[][] components = new Component[creator.getColumnCount()][20];
        for (int i = 0; i < components.length; i++) {
            components[i][0] = columns.get(i).getName();

            for (int j = 1; j < components[i].length; j++) {
                Column column = columns.get(i);
                if (column.getEntries().size() < j) {
                    components[i][j] = Component.empty();
                    continue;
                }
                components[i][j] = column.getEntries().get(j - 1).getName();
            }
        }
        return components;
    }

    /**
     * Converts a list of columns to the favicons
     *
     * @param columns the columns to convert
     * @return an array with the favicons
     */
    private PlayerInfoUpdatePacket.Property[][] toFavicons(List<Column> columns) {
        PlayerInfoUpdatePacket.Property[][] favicon = new PlayerInfoUpdatePacket.Property[creator.getColumnCount()][20];
        for (int i = 0; i < favicon.length; i++) {
            favicon[i][0] = columns.get(i).getFavicon().getProperty();
            Column column = columns.get(i);
            for (int j = 1; j < favicon[i].length; j++) {
                List<PlayerListEntry> entries = new ArrayList<>(column.getEntries());
                if (entries.size() < j) {
                    favicon[i][j] = PlayerListFavicon.GREY.getProperty();
                    continue;
                }
                favicon[i][j] = entries.get(j - 1).getFavicon();
            }
        }
        return favicon;
    }

    /**
     * A custom equals method for PlayerInfoUpdatePacket.Property
     *
     * @param p1 the first property
     * @param p2 the second property
     * @return true if the properties are not equal, indicating an update is needed
     */
    private boolean faviconNotEquals(PlayerInfoUpdatePacket.Property p1, PlayerInfoUpdatePacket.Property p2) {
        if (!p1.name().equals(p2.name())) {
            return true;
        }
        if (p1.signature() == null || p2.signature() == null) {
            return true;
        }
        if (!p1.signature().equals(p2.signature())) {
            return true;
        }
        return !p1.value().equals(p2.value());
    }
}
