package net.cytonic.cytosis.plugins.loader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.cytonic.cytosis.Cytosis;

/**
 * The per-plugin class loader.
 */
public class PluginClassLoader extends URLClassLoader {

    public static final Set<PluginClassLoader> LOADERS = new CopyOnWriteArraySet<>();

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public PluginClassLoader(URL[] urls) {
        super(urls, PluginClassLoader.class.getClassLoader());
    }

    public void addToClassloaders() {
        LOADERS.add(this);
    }

    public void addPath(Path path) {
        try {
            addURL(path.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void close() throws IOException {
        LOADERS.remove(this);
        super.close();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass0(name, resolve, true);
    }

    private Class<?> loadClass0(String name, boolean resolve, boolean checkOther) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException ignored) {
            // Ignored: we'll try others
        }

        if (checkOther) {
            for (PluginClassLoader loader : LOADERS) {
                if (loader != this) {
                    try {
                        return loader.loadClass0(name, resolve, false);
                    } catch (ClassNotFoundException ignored) {
                        // We're trying others, safe to ignore
                    }
                }
            }
        }

        // last resort, try the parent class loader
        if (getParent() != null) {
            return Cytosis.class.getClassLoader().loadClass(name);
        }

        throw new ClassNotFoundException(name);
    }
}
