package net.cytonic.cytosis.player.trait;

import java.time.Instant;
import java.util.UUID;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.managers.LocalCooldownManager;
import net.cytonic.cytosis.managers.NetworkCooldownManager;

public interface Cooldownable {

    private static LocalCooldownManager local() {
        return Cytosis.get(LocalCooldownManager.class);
    }

    private static NetworkCooldownManager network() {
        return Cytosis.get(NetworkCooldownManager.class);
    }

    UUID getUuid();

    /**
     * Determines if the specified cooldown is active for the player
     *
     * @param namespace the namespace of the cooldown
     * @return the boolean true if the player is on cooldown
     */
    default boolean onNetworkCooldown(Key namespace) {
        return network().isOnPersonalCooldown(getUuid(), namespace);
    }

    /**
     * Determines if the specified cooldown is active for the player, on this server
     *
     * @param namespace the namespace of the local cooldown
     * @return the boolean true if the player is on cooldown on this server
     */
    default boolean onLocalCooldown(Key namespace) {
        return local().isOnPersonalCooldown(getUuid(), namespace);
    }

    /**
     * Sets the player's personal cooldown
     *
     * @param namespace the namespace of the cooldown
     * @param expiry    the instant the cooldown is to expire
     */
    default void setNetworkCooldown(Key namespace, Instant expiry) {
        network().setPersonal(getUuid(), namespace, expiry);
    }

    /**
     * Sets the player's personal local cooldown
     *
     * @param namespace the namespace of the local cooldown
     * @param expiry    the instant the cooldown is to expire
     */
    default void setLocalCooldown(Key namespace, Instant expiry) {
        local().setPersonalCooldown(getUuid(), namespace, expiry);
    }

    /**
     * Gets the expiry of this player's network cooldown
     *
     * @param namespace the namespace of the cooldown
     * @return the instant the specified cooldown expires. May be null if the player isn't on cooldown
     */
    @Nullable
    default Instant getNetworkCooldown(Key namespace) {
        return network().getPersonalExpiry(getUuid(), namespace);
    }

    /**
     * Gets the expiry of this player's local cooldown
     *
     * @param namespace the namespace of the cooldown
     * @return the instant the specified cooldown expires. May be null if the player isn't on cooldown
     */
    @Nullable
    default Instant getLocalCooldown(Key namespace) {
        return local().getPersonalExpiry(getUuid(), namespace);
    }
}
