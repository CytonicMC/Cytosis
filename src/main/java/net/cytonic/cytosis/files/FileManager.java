package net.cytonic.cytosis.files;

import com.moandjiezana.toml.Toml;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileManager {
    private static final Path CONFIG_PATH = Path.of("config.toml");

    private final ExecutorService worker;

    public FileManager() {
        this.worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisIOWorker").uncaughtExceptionHandler((t, e) -> Logger.error(STR."An uncaught exception occoured on the thread: \{t.getName()}", e)).factory());
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
                try {
                    extractResource("config.toml", CONFIG_PATH).whenComplete((file, throwable) -> {
                        if (throwable != null) {
                            Logger.error("An error occoured whilst extracting the config.toml file!", throwable);
                            future.completeExceptionally(throwable);
                            return;
                        }

                        try {
                            Toml toml = new Toml().read(file);
                            CytosisSettings.inportConfig(toml.toMap());
                            future.complete(file);
                        } catch (IllegalStateException e) {
                            Logger.error("An error occoured whilst parsing the config.toml file!", e);
                            future.completeExceptionally(e);
                        }

                    });
                } catch (Exception e) {
                    Logger.error("An error occoured whilst creating the config.toml file!", e);
                    future.completeExceptionally(e);
                }
            } else future.complete(CONFIG_PATH.toFile());
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
                    throw new IllegalStateException(STR."The resource \"\{resource}\" does not exist!");
                }

                OutputStream outputStream = new FileOutputStream(path.toFile());
                byte[] buffer = new byte[1024];
                int length;
                while ((length = stream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                stream.close();
                future.complete(path.toFile());
            } catch (IOException e) {
                Logger.error(STR."An error occured whilst extracting the resource \"\{resource}\"!", e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }
}