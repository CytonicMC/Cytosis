package net.cytonic.cytosis.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;

/**
 * A class handling IO and files
 */
@NoArgsConstructor
public class FileManager implements Bootstrappable {

    private static final Path CONFIG_PATH = Path.of("config.json");

    /**
     * Initializes the necessary files and configurations.
     */
    @Override
    public void init() {
        try {
            createConfigFile();
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().file(CONFIG_PATH.toFile()).build();
            ConfigurationNode node = loader.load();
            ConfigurationTransformation transformation = ConfigurationTransformation.builder().build();
            transformation.apply(node);
            loader.save(node);
            CytosisSettings.importConfig(node);
        } catch (Exception exception) {
            Logger.error("Failed to parse config file!", exception);
        }
    }

    /**
     * Creates the config file if it doesn't exist.
     */
    public void createConfigFile() {
        if (!CONFIG_PATH.toFile().exists()) {
            Logger.info("No config file found, creating...");
            extractResource("config.json", CONFIG_PATH);
        }
    }

    /**
     * Extracts a resource file from the classpath and writes it to the specified path.
     *
     * @param resource The name of the resource file to extract.
     * @param path     The path where the extracted file will be written.
     * @return A CompletableFuture representing the completion of the file extraction process.
     */
    public File extractResource(String resource, Path path) {
        try {
            InputStream stream = FileManager.class.getClassLoader().getResourceAsStream(resource);
            if (stream == null) {
                throw new IllegalStateException("The resource \"" + resource + "\" does not exist!");
            }
            OutputStream outputStream = new FileOutputStream(path.toFile());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = stream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            stream.close();
        } catch (IOException e) {
            Logger.error("An error occurred whilst extracting the resource \"" + resource + "\"!", e);
        }
        return path.toFile();
    }
}