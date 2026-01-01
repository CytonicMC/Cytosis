package net.cytonic.cytosis.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Class Loader that can modify class bytecode when they are loaded
 */
public class CytosisRootClassLoader extends URLClassLoader {

    private static volatile CytosisRootClassLoader INSTANCE;

    private CytosisRootClassLoader(ClassLoader parent) {
        super("Cytosis Root ClassLoader", extractUrlsFromClasspath(), parent);
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
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        try {
            // we do not load system classes by ourselves
            return ClassLoader.getPlatformClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            try {
                return define(name, resolve);
            } catch (Exception ex) {
                // fail to load a class, let parent load
                // this forbids code modification, but at least it will load
                return super.loadClass(name, resolve);
            }
        }
    }

    private Class<?> define(String name, boolean resolve) throws IOException, ClassNotFoundException {
        byte[] bytes = loadBytes(name);
        Class<?> defined = defineClass(name, bytes, 0, bytes.length);
        if (resolve) {
            resolveClass(defined);
        }
        return defined;
    }

    public byte[] loadBytes(String name) throws IOException, ClassNotFoundException {
        if (name == null) throw new ClassNotFoundException();
        String path = name.replace(".", "/") + ".class";

        byte[] originalBytes;
        try (InputStream input = getResourceAsStream(path)) {
            if (input == null) throw new ClassNotFoundException("Could not find resource " + path);
            originalBytes = input.readAllBytes();
        }

        return originalBytes;
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