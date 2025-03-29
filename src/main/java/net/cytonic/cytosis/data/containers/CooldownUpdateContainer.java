package net.cytonic.cytosis.data.containers;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.key.Key;

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
    private Key namespace = null;
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
        return id + "-" + new Gson().toJson(this);
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
        return new Gson().fromJson(json, CooldownUpdateContainer.class);
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
