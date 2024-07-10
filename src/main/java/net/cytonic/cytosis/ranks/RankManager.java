package net.cytonic.cytosis.ranks;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.events.ranks.RankChangeEvent;
import net.cytonic.cytosis.events.ranks.RankSetupEvent;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.enums.PlayerRank;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.permission.Permission;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that manages player ranks
 */
public class RankManager {

    private final ConcurrentHashMap<UUID, PlayerRank> rankMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PlayerRank, Team> teamMap = new ConcurrentHashMap<>();
    /**
     * Default constructor
     */
    public RankManager() {
        // Do nothing
    }

    /**
     * Creates the teams for cosmetic ranks
     */
    public void init() {
        for (PlayerRank value : PlayerRank.values()) {
            Team team = new TeamBuilder(value.ordinal() + value.name(), MinecraftServer.getTeamManager())
                    .collisionRule(TeamsPacket.CollisionRule.NEVER)
                    .teamColor(value.getTeamColor())
                    .prefix(value.getPrefix().appendSpace())
                    .build();
            teamMap.put(value, team);
        }
    }

    /**
     * Adds a player to the rank manager
     *
     * @param player the player
     */
    public void addPlayer(Player player) {
        // cache the rank
        Cytosis.getDatabaseManager().getMysqlDatabase().getPlayerRank(player.getUuid()).whenComplete((playerRank, throwable) -> {
            if (throwable != null) {
                Logger.error(STR."An error occured whilst fetching \{player.getUsername()}'s rank!", throwable);
                return;
            }
            var event = new RankSetupEvent(player, playerRank);
            EventDispatcher.call(event);
            if (event.isCanceled()) return;
            rankMap.put(player.getUuid(), playerRank);
            setupCosmetics(player, playerRank);
        });
    }

    /**
     * Changes a players rank
     *
     * @param player the player
     * @param rank   the rank
     */
    public void changeRank(Player player, PlayerRank rank) {
        if (!rankMap.containsKey(player.getUuid()))
            throw new IllegalStateException(STR."The player \{player.getUsername()} is not yet initialized! Call addPlayer(Player) first!");
        PlayerRank old = rankMap.get(player.getUuid());
        var event = new RankChangeEvent(old, rank, player);
        EventDispatcher.call(event);
        if (event.isCanceled()) return;
        removePermissions(player, old.getPermissions());
        rankMap.put(player.getUuid(), rank);
        setupCosmetics(player, rank);
        if (Cytosis.getCytonicNetwork() != null)
            Cytosis.getCytonicNetwork().updatePlayerRank(player.getUuid(), rank);
    }

    /**
     * Sets up the cosmetics. (Team, tab list, etc.)
     * @param player The player
     * @param rank   The rank
     */
    private void setupCosmetics(Player player, PlayerRank rank) {
        addPermissions(player, rank.getPermissions());
        teamMap.get(rank).addMember(player.getUsername());
        player.setCustomName(rank.getPrefix().appendSpace().append(player.getName()));
        Cytosis.getCommandHandler().recalculateCommands(player);
    }

    /**
     * Removes a player from the manager. It also strips permissions
     *
     * @param player The player
     */
    public void removePlayer(Player player) {
        removePermissions(player, rankMap.getOrDefault(player.getUuid(), PlayerRank.DEFAULT).getPermissions());
        rankMap.remove(player.getUuid());
    }

    /**
     * Adds permissions to a player
     *
     * @param player the player
     * @param nodes  the permission nodes
     */
    public final void addPermissions(@NotNull final Player player, @NotNull final String... nodes) {
        for (String node : nodes) player.addPermission(new Permission(node));
    }

    /**
     * Strips permissions from a player
     *
     * @param player the player
     * @param nodes  the nodes to remove
     */
    public final void removePermissions(@NotNull final Player player, @NotNull final String... nodes) {
        for (String node : nodes) player.removePermission(node);
    }

    /**
     * Gets a player's rank
     *
     * @param uuid The uuid of the player
     * @return the player's Rank, if it exists
     */
    public Optional<PlayerRank> getPlayerRank(UUID uuid) {
        return Optional.ofNullable(rankMap.get(uuid));
    }
}