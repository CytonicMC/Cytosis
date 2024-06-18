package net.cytonic.cytosis.plugins.loader;

import net.cytonic.cytosis.logging.Logger;

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
     */
    @Override
    public Class<?> loadClass(String name) {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            Logger.error(STR."An error occured whilst loading plugin class: \{name}", e);
            return null;
        }
    }
}
