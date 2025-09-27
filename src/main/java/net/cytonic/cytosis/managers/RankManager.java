package net.cytonic.cytosis.managers;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NoArgsConstructor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandHandler;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;

/**
 * A class that manages player ranks
 */
@NoArgsConstructor
public class RankManager implements Bootstrappable {

    private final ConcurrentHashMap<UUID, PlayerRank> rankMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PlayerRank, Team> teamMap = new ConcurrentHashMap<>();
    private CytonicNetwork cytonicNetwork;
    private RedisDatabase redis;
    private MysqlDatabase db;

    /**
     * Creates the teams for cosmetic ranks
     */
    @Override
    public void init() {
        this.cytonicNetwork = Cytosis.CONTEXT.getComponent(CytonicNetwork.class);
        this.redis = Cytosis.CONTEXT.getComponent(RedisDatabase.class);
        this.db = Cytosis.CONTEXT.getComponent(MysqlDatabase.class);
        for (PlayerRank value : PlayerRank.values()) {
            Team team = new TeamBuilder(value.ordinal() + value.name(), MinecraftServer.getTeamManager()).collisionRule(
                    TeamsPacket.CollisionRule.NEVER)
                .teamColor(value.getTeamColor())
                .prefix(value.getPrefix())
                .build();
            teamMap.put(value, team);
        }
    }

    /**
     * Adds a player to the rank manager
     *
     * @param player the player
     */
    public void addPlayer(CytosisPlayer player) {
        // cache the rank
        db.getPlayerRank(player.getUuid()).whenComplete((playerRank, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occured whilst fetching " + player.getUsername() + "'s rank!", throwable);
                return;
            }
            player.setRankUnsafe(playerRank);
            rankMap.put(player.getUuid(), playerRank);
            cytonicNetwork.updateCachedPlayerRank(player.getUuid(), playerRank);
            Thread.ofVirtual().start(() -> redis
                .addToHash("player_ranks", player.getUuid()
                    .toString(), playerRank.name()));
            if (player.isNicked()) {
                return; // don't setup cosmetics for nicked players
            }
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
        if (!rankMap.containsKey(player.getUuid())) {
            throw new IllegalStateException(
                "The player " + player.getUsername() + " is not yet initialized! Call addPlayer(Player) first!");
        }

        rankMap.put(player.getUuid(), rank);
        player.setRankUnsafe(rank);
        setupCosmetics(player, rank);
        cytonicNetwork.updateCachedPlayerRank(player.getUuid(), rank);
        player.refreshCommands();
        Thread.ofVirtual().start(() -> redis
            .addToHash("player_ranks", player.getUuid().toString(), rank.name()));
    }

    /**
     * Simply changes a player's rank without sending any packets or doing any other checks. -- The set value isn't
     * persisted
     *
     * @param player The UUID of the player to change the rank of
     * @param rank   the new rank
     */
    public void changeRankSilently(UUID player, PlayerRank rank) {
        rankMap.put(player, rank);
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
        Cytosis.CONTEXT.getComponent(CommandHandler.class).recalculateCommands(player);
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
     * Loads this player's rank from the redis cache
     *
     * @param player the player whose rank to load
     */
    public void loadPlayer(UUID player) {
        Thread.ofVirtual().start(() -> {
            PlayerRank rank = PlayerRank.DEFAULT;
            String cachedRank = redis
                .getFromHash("player_ranks", player.toString());
            if (cachedRank != null) {
                rank = PlayerRank.valueOf(cachedRank);
            } else {
                // we need to load it for next time!
                db.getPlayerRank(player).thenAccept(playerRank -> {
                    rankMap.put(player, playerRank);
                    cytonicNetwork.updateCachedPlayerRank(player, playerRank);
                    redis
                        .addToHash("player_ranks", player.toString(), playerRank.name());
                });
            }
            rankMap.put(player, rank);
        });
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