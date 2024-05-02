package net.cytonic.cytosis.files;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.cytonic.cytosis.logging.Logger;
import java.io.*;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileManager {
    private static final Path OPERATORS_PATH = Path.of("ops.json");

    private final ExecutorService worker;
    private final ConcurrentLinkedQueue<UUID> operators = new ConcurrentLinkedQueue<>();

    public FileManager() {
        this.worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisIOWorker").uncaughtExceptionHandler((t, e) -> Logger.error(STR."An uncaught exception occoured on the thread: \{t.getName()}", e)).factory());
    }

    public void init() {
        worker.submit(this::createAndParseOperatorsFile);
    }

    private void createAndParseOperatorsFile() {
        /*
        The schema of this file is like this:
        {"server_operators":["UUID", "UUID", "UUID"]}
         */
        File operators = OPERATORS_PATH.toFile();
        if (!operators.exists()) {
            try {
                operators.createNewFile();
                JsonWriter writer = new JsonWriter(new FileWriter(operators));
                writer.beginObject();
                writer.name("server_operators");
                writer.beginArray();
                writer.endArray();
                writer.endObject();
                writer.close();
            } catch (IOException e) {
                Logger.error("An error occoured whilst creating the ops.json file!", e);
            }
        }

        InputStream stream;
        try {
            stream = new FileInputStream(operators);
        } catch (FileNotFoundException e) {
            Logger.error("An error occoured whilst converting the ops.json file to a FileInputStream!", e);
            return;
        }

        InputStreamReader streamReader = new InputStreamReader(stream);
        JsonReader reader = new JsonReader(streamReader);

        try {
            reader.beginObject();
            if (reader.nextName().equalsIgnoreCase("server_operators")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    this.operators.add(UUID.fromString(reader.toString()));
                }
                reader.endArray();
            }
            reader.endObject();
            reader.close();
            streamReader.close();
            stream.close();
            Logger.info(STR."Loaded \{this.operators.size()} operators.");
        } catch (Throwable e) {
            Logger.error("An error occoured whilst parsing json the ops.json file!", e);
        }
    }

    public void addOperator(UUID uuid) {
        worker.submit(() -> {
            this.operators.add(uuid);
            File operators = OPERATORS_PATH.toFile();
            if (!operators.exists()) {
                throw new IllegalStateException("The operators file does not exist!");
            }

            try {
                JsonWriter writer = new JsonWriter(new FileWriter(operators));
                writer.beginObject();
                writer.name("server_operators");
                writer.beginArray();
                for (UUID operator : this.operators) writer.value(operator.toString());
                writer.endArray();
                writer.endObject();
                writer.close();
            } catch (IOException e) {
                Logger.error("An error occurred whilst creating the ops.json file!", e);
            }
        });
    }
}