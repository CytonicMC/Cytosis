package net.cytonic.cytosis.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.objects.ExpiringMap;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.playerlist.Column;
import net.cytonic.cytosis.playerlist.PlayerListEntry;
import net.cytonic.cytosis.playerlist.PlayerListFavicon;
import net.cytonic.cytosis.playerlist.PlayerlistCreator;
import net.cytonic.cytosis.server.AbstractCytosisServer;
import net.cytonic.cytosis.server.playerList.PlayerListService;

/**
 * A class that manages the player list
 */
@Setter
@Getter
@CytosisComponent
public class PlayerListManager<P extends CytosisPlayer> implements Bootstrappable {

    private final Map<UUID, Component[][]> playerComponents = new ExpiringMap<>();
    private final Map<UUID, PlayerInfoUpdatePacket.Property[][]> playerFavicons = new ExpiringMap<>();
    private UUID[][] listUuids; // <column, entry>
    private PlayerlistCreator<P> creator;
    private PlayerListService<P> playerListService;
    private TaskSchedule schedule;

    @Override
    public void init() {
        playerListService = Cytosis.<AbstractCytosisServer<P>>getGeneric(AbstractCytosisServer.class)
            .playerListService();
        if (playerListService.supportsPlayerList()) {
            creator = playerListService.creator();
            schedule = playerListService.schedule();

            scheduleUpdate();
            listUuids = new UUID[creator.getColumnCount()][20];
            for (int i = 0; i < listUuids.length; i++) {
                for (int j = 0; j < listUuids[i].length; j++) {
                    listUuids[i][j] = UUID.randomUUID();
                }
            }
        }
    }

    /**
     * Injects the player list packets into the player's connection
     *
     * @param player The player to set up
     */
    public void setupPlayer(P player) {
        if (!playerListService.supportsPlayerList()) {
            player.setListed(false);
            return;
        }

        player.sendPlayerListHeaderAndFooter(creator.header(player), creator.footer(player));
        PacketSendingUtils.broadcastPlayPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
            new PlayerInfoUpdatePacket.Entry(player.getUuid(), player.getUsername(),
                player.getSkin() != null ? List.of(new PlayerInfoUpdatePacket.Property("textures",
                    player.getSkin().textures(), player.getSkin().signature())) : List.of(),
                false, player.getLatency(), player.getGameMode(),
                player.getDisplayName(), null, -1, true)));

        player.sendPackets(createInjectPackets(player));
    }

    /**
     * Creates the player list packets
     *
     * @return the list of packets
     */
    private List<SendablePacket> createInjectPackets(P player) {
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
                new PlayerInfoUpdatePacket.Entry(p.getUuid(), p.getUsername(),
                    p.getSkin() != null ? List.of(new PlayerInfoUpdatePacket.Property("textures",
                        p.getSkin().textures(), p.getSkin().signature())) : List.of(),
                    false, p.getLatency(), p.getGameMode(), p.getDisplayName(), null, -1, true)));
        }
        return packets;
    }

    /**
     * Update a player's player list
     *
     * @param player the player to update
     */
    public void update(P player) {
        if (!playerListService.supportsPlayerList()) return;

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
     * Updates all player lists
     */
    public void updateAll() {
        //noinspection unchecked
        Cytosis.getOnlinePlayers().forEach(p -> update((P) p));
    }

    /**
     * Schedules the next update
     */
    private void scheduleUpdate() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            updateAll();
            scheduleUpdate();
        }).delay(schedule).schedule();
    }

    /**
     * Creates and returns a list of update packets to refresh the player list based on the provided updated components,
     * updated favicons, and columns. This method compares the current state of the player list data for the specified
     * player with the provided updated data and generates packets to reflect the changes.
     *
     * @param player            the unique identifier of the player whose player list is being updated
     * @param updatedComponents a 2D array of components representing the updated display names in the player list
     * @param updatedFavicons   a 2D array of properties representing the updated favicons (player head textures) in the
     *                          player list
     * @param columns           a list of column objects representing the data structure of the player list
     * @return a list of {@code SendablePacket} objects representing the necessary updates to the player list
     */
    private List<SendablePacket> createUpdatePackets(UUID player, Component[][] updatedComponents,
        Property[][] updatedFavicons,
        List<Column> columns) {
        List<SendablePacket> updatePackets = new ArrayList<>();
        PlayerInfoUpdatePacket.Property[][] favicons = playerFavicons.getOrDefault(player, updatedFavicons);
        Component[][] components = playerComponents.getOrDefault(player, updatedComponents);
        int order = 0;
        for (int i = 0; i < updatedComponents.length; i++) {
            processColumnEntry(updatePackets, favicons, components, updatedFavicons, updatedComponents,
                columns, i, 0, order, true);

            for (int j = 1; j < updatedComponents[i].length; j++) {
                processColumnEntry(updatePackets, favicons, components, updatedFavicons, updatedComponents,
                    columns, i, j, order, false);
            }
        }
        playerFavicons.put(player, updatedFavicons);
        playerComponents.put(player, updatedComponents);
        return updatePackets;
    }

    /**
     * Processes a single entry in the player list and creates update packets if needed
     *
     * @param updatePackets     the list to add update packets to
     * @param favicons          the current favicons
     * @param components        the current components
     * @param updatedFavicons   the updated favicons
     * @param updatedComponents the updated components
     * @param columns           the columns to compare with
     * @param i                 the column index
     * @param j                 the entry index
     * @param order             the order value for the entry
     * @param isHeader          whether this entry is a column header
     */
    private void processColumnEntry(List<SendablePacket> updatePackets,
        PlayerInfoUpdatePacket.Property[][] favicons,
        Component[][] components,
        PlayerInfoUpdatePacket.Property[][] updatedFavicons,
        Component[][] updatedComponents,
        List<Column> columns,
        int i, int j, int order, boolean isHeader) {
        // Check if favicon needs updating
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

        // Check if component needs updating
        boolean componentNeedsUpdate = isHeader
            ? !components[i][j].equals(columns.get(i).getName())
            : !components[i][j].equals(updatedComponents[i][j]);

        if (componentNeedsUpdate) {
            if (!isHeader) {
                components[i][j] = updatedComponents[i][j];
            }
            updatePackets.add(new PlayerInfoUpdatePacket(
                PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                new PlayerInfoUpdatePacket.Entry(listUuids[i][j],
                    "!" + (char) ('A' + i) + "-" + (char) ('a' + j),
                    List.of(updatedFavicons[i][j]), true, 1, GameMode.CREATIVE, updatedComponents[i][j], null,
                    order, true)
            ));
        }
    }

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
