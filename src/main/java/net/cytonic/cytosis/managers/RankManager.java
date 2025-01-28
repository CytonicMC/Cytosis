package net.cytonic.cytosis.managers;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that manages player ranks
 */
@NoArgsConstructor
public class RankManager {

    private final MysqlDatabase db = Cytosis.getDatabaseManager().getMysqlDatabase();

    private final ConcurrentHashMap<UUID, PlayerRank> rankMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PlayerRank, Team> teamMap = new ConcurrentHashMap<>();

    /**
     * Creates the teams for cosmetic ranks
     */
    public void init() {
        for (PlayerRank value : PlayerRank.values()) {
            Team team = new TeamBuilder(value.ordinal() + value.name(), MinecraftServer.getTeamManager())
                    .collisionRule(TeamsPacket.CollisionRule.NEVER)
                    .teamColor(value.getTeamColor())
                    .prefix(value.getPrefix())
                    .build();
            teamMap.put(value, team);
        }

        PreparedStatement ps = db.prepare("SELECT * FROM cytonic_ranks");
        db.query(ps).whenComplete((resultSet, throwable) -> {
            if (throwable != null) {
                Logger.error(" ===== FATAL: Failed to load player ranks =====", throwable);
                MinecraftServer.stopCleanly();
                return;
            }

            try {
                while (resultSet.next()) {
                    rankMap.put(UUID.fromString(resultSet.getString("uuid")), PlayerRank.valueOf(resultSet.getString("rank_id")));
                }
            } catch (SQLException ex) {
                Logger.error(" ===== FATAL: Failed to load player ranks =====", ex);
                MinecraftServer.stopCleanly();
                return;
            }
        });
    }

    /**
     * Adds a player to the rank manager
     *
     * @param player the player
     */
    public void addPlayer(CytosisPlayer player) {
        // cache the rank
        Cytosis.getDatabaseManager().getMysqlDatabase().getPlayerRank(player.getUuid()).whenComplete((playerRank, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occured whilst fetching " + player.getUsername() + "'s rank!", throwable);
                return;
            }
            player.setRank_UNSAFE(playerRank);
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
    public void changeRank(CytosisPlayer player, PlayerRank rank) {
        if (!rankMap.containsKey(player.getUuid()))
            throw new IllegalStateException("The player " + player.getUsername() + " is not yet initialized! Call addPlayer(Player) first!");
        PlayerRank old = rankMap.get(player.getUuid());

        rankMap.put(player.getUuid(), rank);
        player.setRank_UNSAFE(rank);
        setupCosmetics(player, rank);
        if (Cytosis.getCytonicNetwork() != null)
            Cytosis.getCytonicNetwork().updatePlayerRank(player.getUuid(), rank);
        player.sendPacket(Cytosis.getCommandManager().createDeclareCommandsPacket(player));
    }

    /**
     * Sets up the cosmetics. (Team, tab list, etc.)
     *
     * @param player The player
     * @param rank   The rank
     */
    public void setupCosmetics(CytosisPlayer player, PlayerRank rank) {
        teamMap.get(rank).addMember(player.getUsername());
        player.setCustomName(rank.getPrefix().append(player.getName()));
        Cytosis.getCommandHandler().recalculateCommands(player);
        if (player.isVanished()) {
            player.setVanished(true); // ranks can mess up the visuals sometimes
        }
    }

    /**
     * Removes a player from the manager.
     *
     * @param player The player
     */
    public void removePlayer(UUID player) {
        rankMap.remove(player);
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