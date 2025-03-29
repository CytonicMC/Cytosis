package net.cytonic.cytosis.plugins.annotations;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Getter
public class SerializedDescription {
    public static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{0,63}");

    // @Nullable is used here to make GSON skip these in the serialized file
    private final String id;
    private final @Nullable String name;
    private final @Nullable String version;
    private final @Nullable String description;
    private final @Nullable List<String> authors;
    private final @Nullable List<Dependency> dependencies;
    private final String main;

    private SerializedDescription(String id, String name, String version, String description, List<String> authors, List<Dependency> dependencies, String main) {
        Preconditions.checkNotNull(id, "id");
        Preconditions.checkArgument(ID_PATTERN.matcher(id).matches(), "id is not valid");
        this.id = id;
        this.name = Strings.emptyToNull(name);
        this.version = Strings.emptyToNull(version);
        this.description = Strings.emptyToNull(description);
        this.authors = authors == null ? ImmutableList.of() : ImmutableList.copyOf(authors);
        this.dependencies = dependencies == null || dependencies.isEmpty() ? ImmutableList.of() : dependencies;
        this.main = Preconditions.checkNotNull(main, "main");
    }

    static SerializedDescription from(Plugin plugin, String qualifiedName) {
        List<Dependency> dependencies = new ArrayList<>();
        for (net.cytonic.cytosis.plugins.annotations.Dependency dependency : plugin.dependencies()) {
            dependencies.add(new Dependency(dependency.id(), dependency.optional()));
        }
        return new SerializedDescription(plugin.name(), plugin.name(), plugin.version(), plugin.description(), List.of(plugin.authors()), dependencies, qualifiedName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SerializedDescription that = (SerializedDescription) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(description, that.description) && Objects.equals(authors, that.authors) && Objects.equals(dependencies, that.dependencies) && Objects.equals(main, that.main);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, version, description, authors, dependencies);
    }

    @Override
    public String toString() {
        return "SerializedPluginDescription{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", version='" + version + '\''
                + ", description='" + description + '\''
                + ", authors=" + authors
                + ", dependencies=" + dependencies
                + ", main='" + main + '\''
                + '}';
    }

    /**
     * Represents a dependency.
     */
    public record Dependency(String id, boolean optional) {

        @Override
        public String toString() {
            return "Dependency{"
                    + "id='" + id + '\''
                    + ", optional=" + optional
                    + '}';
        }
    }
}
