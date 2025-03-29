package net.cytonic.cytosis.plugins.java;

import lombok.Getter;
import net.cytonic.cytosis.plugins.PluginDescription;
import net.cytonic.cytosis.plugins.dependencies.PluginDependency;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class JavaPluginDescription extends PluginDescription {

    private final Class<?> mainClass;

    public JavaPluginDescription(String id, @Nullable String name, @Nullable String version,
                                 @Nullable String description,
                                 @Nullable List<String> authors, Collection<PluginDependency> dependencies, Path source,
                                 Class<?> mainClass) {
        super(id, name, version, description, authors, dependencies, source);
        this.mainClass = checkNotNull(mainClass);
    }
}
