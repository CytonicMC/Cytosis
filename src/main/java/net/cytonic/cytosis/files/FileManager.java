package net.cytonic.cytosis.files;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A class handling IO and files
 */
@NoArgsConstructor
public class FileManager {

    private static final Path CONFIG_PATH = Path.of("config.toml");

    /**
     * Initializes the necessary files and configurations.
     */
    public void init() {
        createConfigFile();
        try {
            parseToml(Toml.parse(CONFIG_PATH));
        } catch (IOException e) {
            Logger.error("Failed to parse config file: " + CONFIG_PATH, e);
        }
    }

    /**
     * Creates the config file if it doesn't exist.
     *
     * @return A CompletableFuture representing the completion of the file creation process.
     */
    public File createConfigFile() {
        if (!CONFIG_PATH.toFile().exists()) {
            Logger.info("No config file found, creating...");
            return extractResource("config.toml", CONFIG_PATH);
        }
        return CONFIG_PATH.toFile();
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
            while ((length = stream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
            outputStream.close();
            stream.close();
        } catch (IOException e) {
            Logger.error("An error occured whilst extracting the resource \"" + resource + "\"!", e);
        }
        return path.toFile();
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