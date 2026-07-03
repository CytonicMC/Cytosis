package net.cytonic.cytosis.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;

public class ResourceUtils {

    /**
     * Loads a JSON file This method works both when running from a JAR file and from the file system.
     * <p>Note: Callers are responsible for closing the returned InputStreamReader
     *
     * @param path The file path (e.g., "config/settings.json" or "data/templates/template.json")
     * @return InputStreamReader for the specified file
     * @throws IllegalArgumentException if the file is not found
     * @throws RuntimeException         if there's an error reading the file
     */
    public static InputStreamReader getResourceFile(String path) {
        ClassLoader cl = Cytosis.class.getClassLoader();

        // Normalize the path (remove leading/trailing slashes)
        String normalizedPath = path.replaceAll("^/+|/+$", "");

        InputStream inputStream = cl.getResourceAsStream(normalizedPath);
        if (inputStream == null) {
            // If not found in classpath resources, try as a file system path
            try {
                Path filePath = Paths.get(normalizedPath);
                if (Files.exists(filePath)) {
                    inputStream = Files.newInputStream(filePath);
                } else {
                    throw new IllegalArgumentException("File not found: " + path);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load file from path: " + path, e);
            }
        }

        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * Loads all JSON files from a resource directory and returns them as InputStreamReaders. This method works both
     * when running from a JAR file and from the file system (IDE).
     *
     * @param path The resource path (e.g., "config" or "data/templates")
     * @return List of InputStreamReaders for all JSON files in the directory
     * @throws IllegalArgumentException if the directory is not found
     * @throws RuntimeException         if there's an error reading the resources
     *                                  <p>
     *                                  Note: Callers are responsible for closing the returned InputStreamReaders
     */
    public static List<InputStreamReader> getResourceFiles(String path) {
        List<InputStreamReader> readers = new ArrayList<>();
        ClassLoader cl = Cytosis.class.getClassLoader();

        // Normalize path (remove leading/trailing slashes)
        String normalizedPath = path.replaceAll("^/+|/+$", "");

        try {
            URL dirURL = cl.getResource(normalizedPath);
            if (dirURL == null) {
                throw new IllegalArgumentException("Directory not found: " + path);
            }

            String protocol = dirURL.getProtocol();

            if ("jar".equals(protocol)) {
                loadFromJar(cl, dirURL, normalizedPath, readers);
            } else if ("file".equals(protocol)) {
                loadFromFileSystem(cl, normalizedPath, readers);
            } else {
                throw new IllegalArgumentException("Unsupported protocol: " + protocol);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load resources from path: " + path, e);
        }

        return readers;
    }

    /**
     * Loads resources from a JAR file
     */
    private static void loadFromJar(ClassLoader cl, URL dirURL, String normalizedPath, List<InputStreamReader> readers)
        throws IOException {

        String jarPath = dirURL.getPath();

        // Extract jar file path (handle both jar:file: and jar: protocols) because aparently its a thing
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring(5); // Remove "file:" prefix
        }

        int jarSeparatorIndex = jarPath.indexOf("!");
        if (jarSeparatorIndex != -1) {
            jarPath = jarPath.substring(0, jarSeparatorIndex);
        }

        try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
            String pathPrefix = normalizedPath + "/";

            for (JarEntry entry : Collections.list(jar.entries())) {
                String name = entry.getName();
                if (entry.isDirectory() ||
                    !name.startsWith(pathPrefix) ||
                    !name.endsWith(".json") ||
                    name.indexOf('/', pathPrefix.length()) != -1) {
                    continue;
                }
                InputStream in = cl.getResourceAsStream(name);
                if (in != null) {
                    readers.add(new InputStreamReader(in, StandardCharsets.UTF_8));
                }
            }
        }
    }

    /**
     * Loads resources from the file system (when running from IDE)
     */
    private static void loadFromFileSystem(ClassLoader cl, String normalizedPath, List<InputStreamReader> readers)
        throws IOException {

        URL dirURL = cl.getResource(normalizedPath);
        if (dirURL == null) {
            return;
        }

        Path dirPath;
        try {
            dirPath = Paths.get(dirURL.toURI());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI for directory: " + dirURL, e);
        }

        try (Stream<Path> files = Files.walk(dirPath, 1)) {
            files.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        String resourcePath = normalizedPath + "/" + path.getFileName().toString();
                        InputStream in = cl.getResourceAsStream(resourcePath);
                        if (in != null) {
                            readers.add(new InputStreamReader(in, StandardCharsets.UTF_8));
                        }
                    } catch (Exception e) {
                        Logger.error("Failed to load resource: " + path, e);
                    }
                });
        }
    }

    public static void extractResourceFolder(String folderPath, Path outputDir) throws IOException, URISyntaxException {
        URI uri = ResourceUtils.class.getResource("/" + folderPath.replaceAll("^/+|/+$", "")).toURI();

        // Handle both JAR and IDE (exploded) environments
        if (uri.getScheme().equals("jar")) {
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                Path sourcePath = fs.getPath("/" + folderPath);
                copyDirectory(sourcePath, outputDir);
            }
        } else {
            // Running from IDE or exploded directory
            copyDirectory(Path.of(uri), outputDir);
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(src -> {
                Path dest = target.resolve(source.relativize(src).toString());
                try {
                    if (Files.isDirectory(src)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}
