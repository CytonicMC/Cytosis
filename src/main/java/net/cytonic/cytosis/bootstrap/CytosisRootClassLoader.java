package net.cytonic.cytosis.bootstrap;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import net.cytonic.cytosis.utils.BuildInfo;

/**
 * Class Loader that can modify class bytecode when they are loaded
 */
public class CytosisRootClassLoader extends URLClassLoader {

    private static volatile CytosisRootClassLoader INSTANCE;

    private CytosisRootClassLoader(ClassLoader parent) {
        super("Cytosis Root ClassLoader", extractUrlsFromClasspath(),
            BuildInfo.DEPENDENCIES_BUNDLED ? parent : ClassLoader.getPlatformClassLoader());
    }

    public static CytosisRootClassLoader getInstance() {
        if (INSTANCE == null) {
            synchronized (CytosisRootClassLoader.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CytosisRootClassLoader(CytosisRootClassLoader.class.getClassLoader());
                }
            }
        }
        return INSTANCE;
    }

    private static URL[] extractUrlsFromClasspath() {
        String classpath = System.getProperty("java.class.path");
        String[] parts = classpath.split(";");
        URL[] urls = new URL[parts.length];
        for (int i = 0; i < urls.length; i++) {
            try {
                String part = parts[i];
                String protocol;
                if (part.contains("!")) {
                    protocol = "jar://";
                } else {
                    protocol = "file://";
                }
                urls[i] = URI.create(protocol + part).toURL();
            } catch (MalformedURLException e) {
                throw new Error(e);
            }
        }
        return urls;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    // overridden to increase access (from protected to public)
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}