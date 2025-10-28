package net.cytonic.cytosis.data.packets;

import net.kyori.adventure.key.Key;

import java.time.Instant;
import java.util.UUID;

/**
 * A class that represents a packet for updating cooldowns
 */
public record CooldownUpdatePacket(CooldownTarget target, Key namespace, Instant expiry,
                                   UUID userUuid) implements Packet {
    /**
     * An enum that represents a target for a cooldown.
     */
    public enum CooldownTarget {
        /**
         * One per player
         */
        PERSONAL,
        /**
         * One for the entire network
         */
        GLOBAL
    }
}
