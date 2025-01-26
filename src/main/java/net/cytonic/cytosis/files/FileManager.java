package net.cytonic.cytosis.files;

import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class handling IO and files
 */
public class FileManager {

    private static final Path CONFIG_PATH = Path.of("config.toml");
    private final ExecutorService worker;

    /**
     * The default constructor that initializes the worker thread
     */
    public FileManager() {
        this.worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisIOWorker").uncaughtExceptionHandler((t, e) -> Logger.error("An uncaught exception occoured on the thread: " + t.getName() + "", e)).factory());
    }

    /**
     * Initializes the necessary files and configurations.
     *
     * @return A CompletableFuture representing the completion of the initialization process.
     */
    public CompletableFuture<Void> init() {
        // The create methods invoke the worker thread to create the file, so no need to here.
        return CompletableFuture.allOf(
                createConfigFile()
        );
    }

    /**
     * Creates the config file if it doesn't exist.
     *
     * @return A CompletableFuture representing the completion of the file creation process.
     */
    public CompletableFuture<File> createConfigFile() {
        CompletableFuture<File> future = new CompletableFuture<>();
        worker.submit(() -> {
            if (!CONFIG_PATH.toFile().exists()) {
                Logger.info("No config file found, creating...");
                try {
                    extractResource("config.toml", CONFIG_PATH).whenComplete((file, throwable) -> {
                        if (throwable != null) {
                            Logger.error("An error occurred whilst extracting the config.toml file!", throwable);
                            future.completeExceptionally(throwable);
                            return;
                        }
                        try {
                            parseToml(Toml.parse(CONFIG_PATH));
                            future.complete(file);
                        } catch (IllegalStateException | IOException e) {
                            Logger.error("An error occurred whilst parsing the config.toml file!", e);
                            future.completeExceptionally(e);
                        }
                    });
                } catch (Exception e) {
                    Logger.error("An error occurred whilst creating the config.toml file!", e);
                    future.completeExceptionally(e);
                }
            } else {
                try {
                    parseToml(Toml.parse(CONFIG_PATH));
                } catch (IOException e) {
                    Logger.error("An error occurred whilst parsing the config.toml file!", e);
                    future.completeExceptionally(e);
                }
                future.complete(CONFIG_PATH.toFile());
            }
        });
        return future;
    }

    /**
     * Extracts a resource file from the classpath and writes it to the specified path.
     *
     * @param resource The name of the resource file to extract.
     * @param path     The path where the extracted file will be written.
     * @return A CompletableFuture representing the completion of the file extraction process.
     */
    public CompletableFuture<File> extractResource(String resource, Path path) {
        CompletableFuture<File> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                InputStream stream = FileManager.class.getClassLoader().getResourceAsStream(resource);
                if (stream == null) {
                    throw new IllegalStateException("The resource \"" + resource + "\" does not exist!");
                }
                OutputStream outputStream = new FileOutputStream(path.toFile());
                byte[] buffer = new byte[1024];
                int length;
                while ((length = stream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
                outputStream.close();
                stream.close();
                future.complete(path.toFile());
            } catch (IOException e) {
                Logger.error("An error occured whilst extracting the resource \"" + resource + "\"!", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Extracts a resource file from the classpath and writes it to the specified path.
     *
     * @param stream The {@link InputStream} of a resource file to extract.
     * @param path     The path where the extracted file will be written.
     * @return A CompletableFuture representing the completion of the file extraction process.
     */
    public CompletableFuture<File> extractResource(InputStream stream, Path path) {
        CompletableFuture<File> future = new CompletableFuture<>();
        worker.submit(() -> {
            try {
                if (stream == null) {
                    throw new IllegalStateException("The InputStream is null!");
                }
                OutputStream outputStream = new FileOutputStream(path.toFile());
                byte[] buffer = new byte[1024];
                int length;
                while ((length = stream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
                outputStream.close();
                stream.close();
                future.complete(path.toFile());
            } catch (IOException e) {
                Logger.error("An error occured whilst extracting a resource", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private void parseToml(TomlParseResult toml) {
        if (!toml.errors().isEmpty()) {
            Logger.error("An error occurred whilst parsing the config.toml file!", toml.errors().getFirst());
            return;
        }
        Map<String, Object> config = recursiveParse(toml.toMap(), "");
        CytosisSettings.inportConfig(config);
    }

    private Map<String, Object> recursiveParse(Map<String, Object> map, String parentKey) {
        if (!parentKey.equalsIgnoreCase("")) {
            parentKey = parentKey + ".";
        }
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = parentKey + entry.getKey();
            Object value = entry.getValue();
            // If the value is a nested table (another map), recurse
            if (value instanceof TomlTable toml) {
                resultMap.putAll(recursiveParse(toml.toMap(), key));
            }
            // If it's a list, check for nested tables within the list
            else if (value instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    if (item instanceof TomlTable toml) resultMap.putAll(recursiveParse(toml.toMap(), key));
                }
            } else {
                resultMap.put(key, value);
            }
        }
        return resultMap;
    }
}