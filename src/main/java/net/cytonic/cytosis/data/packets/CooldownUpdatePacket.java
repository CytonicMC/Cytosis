package net.cytonic.cytosis.data.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;

import java.time.Instant;
import java.util.UUID;

/**
 * A class that represents a nats packet for updating cooldowns
 */
@Getter
@Setter
@AllArgsConstructor
public class CooldownUpdatePacket implements Packet {
    private CooldownTarget target;
    private Key namespace;
    private Instant expiry;
    private UUID userUuid;

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
