package net.cytonic.cytosis.bootstrap;

import java.net.URL;
import java.net.URLClassLoader;

public class BootstrapClassLoader extends URLClassLoader {


    public BootstrapClassLoader() {
        super("Bootstrap Class Loader", new URL[]{}, BootstrapClassLoader.class.getClassLoader());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("java.") || name.startsWith("javax.")) {
            return super.loadClass(name, resolve); // delegate to bootstrap for core classes
        }

        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    c = findClass(name); // load from our JARs
                } catch (ClassNotFoundException e) {
                    c = super.loadClass(name, resolve);
                }
            }
            if (resolve) resolveClass(c);
            return c;
        }
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}
