package net.cytonic.cytosis.plugins.dependencies;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import lombok.Getter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

/**
 * Represents a dependency on another plugin.
 */
public final class PluginDependency {

    @Getter
    private final String id;
    private final @Nullable String version;
    @Getter
    private final boolean optional;

    /**
     * Creates a new instance.
     *
     * @param id       the plugin ID
     * @param version  an optional version
     * @param optional whether this dependency is optional
     */
    public PluginDependency(String id, @Nullable String version, boolean optional) {
        this.id = checkNotNull(id, "id");
        checkArgument(!id.isEmpty(), "id cannot be empty");
        this.version = emptyToNull(version);
        this.optional = optional;
    }

    /**
     * Returns the version this {@link PluginDependency} should match.
     *
     * @return an {@link Optional} with the plugin version, may be empty
     */
    public Optional<String> getVersion() {
        return Optional.ofNullable(version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, optional);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PluginDependency that = (PluginDependency) o;
        return optional == that.optional && Objects.equals(id, that.id) && Objects.equals(version, that.version);
    }

    @Override
    public String toString() {
        return "PluginDependency{" + "id='" + id + '\'' + ", version='" + version + '\'' + ", optional=" + optional
            + '}';
    }
}
