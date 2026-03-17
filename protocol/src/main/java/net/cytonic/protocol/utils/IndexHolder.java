package net.cytonic.protocol.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;

@Slf4j
public class IndexHolder {

    private static IndexHolder instance;
    private final IndexView compositeIndex;

    private IndexHolder(List<File> pluginJars) throws IOException {
        long start = System.nanoTime();
        List<IndexView> indices = new ArrayList<>();

        // Add cytosis' index (from resources)
        try (InputStream is = getClass().getResourceAsStream("/META-INF/jandex.idx")) {
            if (is != null) indices.add(new IndexReader(is).read());
        }

        try (InputStream is = IndexHolder.class.getResourceAsStream("/META-INF/protocol-jandex.idx")) {
            if (is != null) indices.add(new IndexReader(is).read());
        }

        if (Boolean.parseBoolean(System.getProperty("cytosis.dependencies_bundled"))) {
            // Fat jar - Minestom is shadowed into our own jar, scan it with a filter
            File selfJar = new File(IndexHolder.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath());
            if (selfJar.getName().endsWith(".jar")) {
                indices.add(indexJar(selfJar, "net.minestom.server.event"));
                indices.add(indexJar(selfJar, "io.github.togar2.pvp.events"));
            }

        } else {
            // Thin jar - Minestom was downloaded at runtime, find the jar
            indices.add(indexJar(findJarForClass("net.minestom.server.event.Event"), "net.minestom.server.event"));
            indices.add(indexJar(findJarForClass("io.github.togar2.pvp.MinestomPvP"), "io.github.togar2.pvp.events"));
        }

        // Add each plugin's index
        for (File jar : pluginJars) {
            indices.add(indexJar(jar));
        }

        this.compositeIndex = CompositeIndex.create(indices);
        log.info(String.format("Built indices in %.2fms", (System.nanoTime() - start) / 1.0e6));
    }

    private static IndexView indexJar(File jarFile) {
        return indexJar(jarFile, "");
    }

    private static IndexView indexJar(File jarFile, String packagePrefix) {
        // Convert package prefix to jar path format once
        String pathPrefix = packagePrefix.replace('.', '/');

        try (JarFile jar = new JarFile(jarFile)) {
            ZipEntry entry = jar.getEntry("META-INF/jandex.idx");
            if (entry != null) {
                try (InputStream is = jar.getInputStream(entry)) {
                    return new IndexReader(is).read();
                }
            }

            // Collect matching entries first (iteration is cheap)
            List<JarEntry> matchingEntries = jar.stream()
                .filter(e -> e.getName().startsWith(pathPrefix) && e.getName().endsWith(".class"))
                .toList();

            // Index each in parallel, each thread gets its own Indexer
            List<IndexView> partialIndices = matchingEntries.parallelStream()
                .map(jarEntry -> {
                    Indexer indexer = new Indexer();
                    try (InputStream is = jar.getInputStream(jarEntry)) {
                        indexer.index(is);
                        return indexer.complete();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }).collect(Collectors.toList());

            // Merge all partial indices into one
            return CompositeIndex.create(partialIndices);

        } catch (IOException e) {
            log.error("Failed to index jar file: ", e);
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private static File findJarForClass(String clazz) {
        return new File(Class.forName(clazz)
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath());
    }

    public static synchronized void initialize(List<File> pluginJars) throws IOException {
        if (instance != null) throw new IllegalStateException("Already initialized");
        instance = new IndexHolder(pluginJars);
    }

    public static IndexView get() {
        if (instance == null) throw new IllegalStateException("Not yet initialized");
        return instance.compositeIndex;
    }
}
