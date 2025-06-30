package net.cytonic.cytosis.plugins.loader;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.plugins.CytosisPlugin;
import net.cytonic.cytosis.plugins.PluginContainer;
import net.cytonic.cytosis.plugins.PluginDescription;
import net.cytonic.cytosis.plugins.annotations.SerializedDescription;
import net.cytonic.cytosis.plugins.dependencies.PluginDependency;
import net.cytonic.cytosis.plugins.java.JavaDescriptionCandidate;
import net.cytonic.cytosis.plugins.java.JavaPluginDescription;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Implements loading a Java plugin.
 */
@NoArgsConstructor
public class JavaPluginLoader {

    private static PluginDependency toDependencyMeta(SerializedDescription.Dependency dependency) {
        return new PluginDependency(dependency.id(), null, dependency.optional());
    }

    public PluginDescription loadCandidate(Path source) throws Exception {
        Optional<SerializedDescription> serialized = getSerializedPluginInfo(source);

        if (serialized.isEmpty()) {
            throw new net.cytonic.cytosis.plugins.loader.PluginLoadError("Did not find a valid cytosis-plugin.json.");
        }

        SerializedDescription pd = serialized.get();
        assert pd.getId() != null;
        if (!SerializedDescription.ID_PATTERN.matcher(pd.getId()).matches()) {
            throw new net.cytonic.cytosis.plugins.loader.PluginLoadError("Plugin ID '" + pd.getId() + "' is invalid.");
        }

        assert pd.getDependencies() != null;
        for (SerializedDescription.Dependency dependency : pd.getDependencies()) {
            if (!SerializedDescription.ID_PATTERN.matcher(dependency.id()).matches()) {
                throw new net.cytonic.cytosis.plugins.loader.PluginLoadError("Dependency ID '" + dependency.id() + "' for plugin '" + pd.getName() + "' is invalid.");
            }
        }

        return createCandidateDescription(pd, source);
    }

    public PluginDescription createPluginFromCandidate(PluginDescription candidate) throws Exception {
        if (!(candidate instanceof JavaDescriptionCandidate candidateInst)) {
            throw new IllegalArgumentException("Description provided isn't of the Java plugin loader");
        }

        URL pluginJarUrl = candidate.getSource().orElseThrow(() -> new net.cytonic.cytosis.plugins.loader.PluginLoadError("Description provided does not have a source path")).toUri().toURL();
        PluginClassLoader loader = new PluginClassLoader(new URL[]{pluginJarUrl});
        loader.addToClassloaders();

        Class<?> mainClass = loader.loadClass(candidateInst.getMainClass());
        return createDescription(candidateInst, mainClass);
    }

    public void createPlugin(PluginContainer container) {
        PluginDescription description = container.getDescription();
        if (!(description instanceof JavaPluginDescription)) {
            throw new IllegalArgumentException("Description provided isn't of the Java plugin loader");
        }

        Class<?> rawMain = ((JavaPluginDescription) description).getMainClass();
        Class<? extends CytosisPlugin> pluginClass;
        try {
            pluginClass = rawMain.asSubclass(CytosisPlugin.class);
        } catch (ClassCastException ignored) {
            throw new net.cytonic.cytosis.plugins.loader.PluginLoadError("Plugin " + description.getName() + " does not implement CytosisPlugin");
        }
        Constructor<? extends CytosisPlugin> constructor;
        try {
            constructor = pluginClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new net.cytonic.cytosis.plugins.loader.PluginLoadError("Plugin " + description.getName() + " does not have a public, no-args constructor.");
        }

        CytosisPlugin plugin;
        try {
            plugin = constructor.newInstance();
        } catch (InstantiationException | InvocationTargetException e) {
            throw new net.cytonic.cytosis.plugins.loader.PluginLoadError("An error occured whilst initializing the plugin!", e);
        } catch (IllegalAccessException e) {
            throw new PluginLoadError("Plugin " + description.getName() + " must have PUBLIC constructor!");
        }


        container.setInstance(plugin);
    }

    private Optional<SerializedDescription> getSerializedPluginInfo(Path source) throws Exception {
        try (JarInputStream in = new JarInputStream(new BufferedInputStream(Files.newInputStream(source)))) {
            JarEntry entry;
            while ((entry = in.getNextJarEntry()) != null) {
                if (entry.getName().equals("cytosis-plugin.json")) {
                    try (Reader pluginInfoReader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                        return Optional.of(Cytosis.GSON.fromJson(pluginInfoReader, SerializedDescription.class));
                    }
                }

            }

            return Optional.empty();
        }
    }

    private PluginDescription createCandidateDescription(SerializedDescription description, Path source) {
        Set<PluginDependency> dependencies = new HashSet<>();

        assert description.getDependencies() != null;
        for (SerializedDescription.Dependency dependency : description.getDependencies()) {
            dependencies.add(toDependencyMeta(dependency));
        }

        return new JavaDescriptionCandidate(description.getName(), description.getName(), description.getVersion(), description.getDescription(), description.getAuthors(), dependencies, source, description.getMain());
    }

    private PluginDescription createDescription(JavaDescriptionCandidate description, Class<?> mainClass) {
        return new JavaPluginDescription(description.getId(), description.getName().orElse(null), description.getVersion().orElse(null), description.getDescription().orElse(null), description.getAuthors(), description.getDependencies(), description.getSource().orElse(null), mainClass);
    }
}
