package net.cytonic.cytosis.ranks;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.events.ranks.RankChangeEvent;
import net.cytonic.cytosis.events.ranks.RankSetupEvent;
import net.cytonic.cytosis.logging.Logger;
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

public class RankManager {
    ConcurrentHashMap<UUID, PlayerRank> rankMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<PlayerRank, Team> teamMap = new ConcurrentHashMap<>();

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

    public void addPlayer(Player player) {
        // cache the rank
        Cytosis.getDatabaseManager().getDatabase().getPlayerRank(player.getUuid()).whenComplete((playerRank, throwable) -> {
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
    }

    private void setupCosmetics(Player player, PlayerRank rank) {
        addPermissions(player, rank.getPermissions());
        teamMap.get(rank).addMember(player.getUsername());
        player.setCustomName(rank.getPrefix().appendSpace().append(player.getName()));
        Cytosis.getCommandHandler().recalculateCommands(player);
    }

    public void removePlayer(Player player) {
        removePermissions(player, rankMap.getOrDefault(player.getUuid(), PlayerRank.DEFAULT).getPermissions());
        rankMap.remove(player.getUuid());
    }

    public final void addPermissions(@NotNull final Player player, @NotNull final String... nodes) {
        for (String node : nodes) player.addPermission(new Permission(node));
    }

    public final void removePermissions(@NotNull final Player player, @NotNull final String... nodes) {
        for (String node : nodes) player.removePermission(node);
    }

    public Optional<PlayerRank> getPlayerRank(UUID uuid) {
        return Optional.ofNullable(rankMap.get(uuid));
    }
}
