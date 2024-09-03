package net.cytonic.cytosis.messaging.containers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.cytonic.cytosis.Cytosis;
import net.minestom.server.utils.NamespaceID;

import java.time.Instant;
import java.util.UUID;

/**
 * A class that represents a redis container for updating cooldowns
 */
@SuppressWarnings("preview")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CooldownUpdateContainer implements Container {

    private final String id = "UPDATE_COOLDOWN";
    private CooldownTarget target = null;
    private NamespaceID namespace = null;
    private Instant expiry = null;
    private UUID userUuid = null;

    /**
     * Creates an instance of this container
     *
     * @return the created object instance
     */
    static CooldownUpdateContainer create() {
        return new CooldownUpdateContainer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize() {
        return STR."\{id}-\{Cytosis.GSON.toJson(this)}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CooldownUpdateContainer parse(String json) {
        return Cytosis.GSON.fromJson(json, CooldownUpdateContainer.class);
    }

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
