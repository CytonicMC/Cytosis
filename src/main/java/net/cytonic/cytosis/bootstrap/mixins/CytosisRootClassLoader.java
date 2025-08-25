package net.cytonic.cytosis.bootstrap.mixins;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public final static Logger LOGGER = LoggerFactory.getLogger(CytosisRootClassLoader.class);
    private static volatile CytosisRootClassLoader INSTANCE;
    private ASMMixinTransformer mixinTransformer;

    private CytosisRootClassLoader(ClassLoader parent) {
        super("Cytosis Root ClassLoader", extractURLsFromClasspath(), parent);
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

    public void initializeMixinTransformer(CytosisMixinService service) {
        this.mixinTransformer = new ASMMixinTransformer(service);
    }

    private static URL[] extractURLsFromClasspath() {
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
        if (loadedClass != null)
            return loadedClass;

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
        byte[] bytes = loadBytes(name, true);
        Class<?> defined = defineClass(name, bytes, 0, bytes.length);
        LOGGER.trace("Loaded with code modifiers: {}", name);
        if (resolve) {
            resolveClass(defined);
        }
        return defined;
    }

    public byte[] loadBytes(String name, boolean transform) throws IOException, ClassNotFoundException {
        if (name == null)
            throw new ClassNotFoundException();
        String path = name.replace(".", "/") + ".class";

        byte[] originalBytes;
        try (InputStream input = getResourceAsStream(path)) {
            if (input == null) {
                throw new ClassNotFoundException("Could not find resource " + path);
            }

            originalBytes = input.readAllBytes();
        }

        // Apply transformations if requested and transformer is available
        if (transform && mixinTransformer != null && mixinTransformer.isValidTarget(name.replace(".", "/"))) {
            try {
                ClassReader reader = new ClassReader(originalBytes);
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, 0);

                // Apply mixin transformation
                if (mixinTransformer.accept(classNode)) {
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                    classNode.accept(writer);
                    return writer.toByteArray();
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to apply mixin transformation to {}: {}", name, e.getMessage());
                // Fall back to original bytes
            }
        }

        return originalBytes;
    }

    // overridden to increase access (from protected to public)
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}