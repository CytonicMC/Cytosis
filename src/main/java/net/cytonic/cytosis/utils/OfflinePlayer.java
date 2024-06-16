package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.ranks.PlayerRank;
import net.minestom.server.permission.Permission;
import net.minestom.server.permission.PermissionHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * An offline player
 *
 * @param name the name of the player
 * @param uuid the uuid of the player
 * @param rank the rank of the player
 */
public record OfflinePlayer(String name, UUID uuid, PlayerRank rank) implements PermissionHandler {

    @Override
    public @NotNull Set<Permission> getAllPermissions() {
        var strings = Set.of(rank.getPermissions());
        Set<Permission> perms = new HashSet<>();
        strings.forEach(s -> perms.add(new Permission(s)));
        return perms;
    }
}
