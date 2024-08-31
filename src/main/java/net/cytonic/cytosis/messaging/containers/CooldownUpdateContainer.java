package net.cytonic.cytosis.messaging.containers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.cytonic.cytosis.Cytosis;
import net.minestom.server.utils.NamespaceID;

import java.time.Instant;
import java.util.UUID;

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

    static CooldownUpdateContainer create() {
        return new CooldownUpdateContainer();
    }

    @Override
    public String serialize() {
        return STR."\{id}-\{Cytosis.GSON.toJson(this)}";
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public CooldownUpdateContainer parse(String json) {
        return Cytosis.GSON.fromJson(json, CooldownUpdateContainer.class);
    }

    public enum CooldownTarget {
        PERSONAL,
        GLOBAL
    }
}
