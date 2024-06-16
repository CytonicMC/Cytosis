package net.cytonic.cytosis.plugins.loader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A classloader for loading plugins
 */
public class PluginClassLoader extends URLClassLoader {

    /**
     * Creates a new plugin classloader
     *
     * @param urls   The urls to load
     * @param parent The parent classloader
     */
    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * Loads a class
     * @param name The name of the class
     * @return The loaded class
     * @throws ClassNotFoundException If the class could not be loaded
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }
}
