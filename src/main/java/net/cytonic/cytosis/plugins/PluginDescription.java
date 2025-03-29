package net.cytonic.cytosis.plugins;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.cytonic.cytosis.plugins.dependencies.PluginDependency;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents metadata for a specific version of a plugin.
 */
public class PluginDescription {

    @Getter
    private final String id;
    private final @Nullable String name;
    private final @Nullable String version;
    private final @Nullable String description;
    @Getter
    private final List<String> authors;
    private final Map<String, PluginDependency> dependencies;
    private final Path source;

    public PluginDescription(String id, @Nullable String name, @Nullable String version, @Nullable String description, @Nullable List<String> authors, Collection<PluginDependency> dependencies, Path source) {
        this.id = checkNotNull(id, "id");
        this.name = Strings.emptyToNull(name);
        this.version = Strings.emptyToNull(version);
        this.description = Strings.emptyToNull(description);
        this.authors = authors == null ? ImmutableList.of() : ImmutableList.copyOf(authors);
        this.dependencies = Maps.uniqueIndex(dependencies, d -> d == null ? null : d.getId());
        this.source = source;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getVersion() {
        return Optional.ofNullable(version);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Collection<PluginDependency> getDependencies() {
        return dependencies.values();
    }

    public Optional<PluginDependency> getDependency(String id) {
        return Optional.ofNullable(dependencies.get(id));
    }

    public Optional<Path> getSource() {
        return Optional.ofNullable(source);
    }

    @Override
    public String toString() {
        return "VelocityPluginDescription{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", version='" + version + '\''
                + ", description='" + description + '\''
                + ", authors=" + authors
                + ", dependencies=" + dependencies
                + ", source=" + source
                + '}';
    }
}
