package net.cytonic.cytosis.plugins.loader;

import net.cytonic.cytosis.Cytosis;

public class AgregatePluginClassloader extends ClassLoader {

    public static final AgregatePluginClassloader INSTANCE = new AgregatePluginClassloader(
        Cytosis.class.getClassLoader());

    public AgregatePluginClassloader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return getParent().loadClass(name);
        } catch (ClassNotFoundException ignored) {
        }

        for (PluginClassLoader loader : PluginClassLoader.LOADERS) {
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(name);
    }
}
