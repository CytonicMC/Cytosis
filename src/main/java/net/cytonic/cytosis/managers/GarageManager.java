package net.cytonic.cytosis.managers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisConfig;
import net.cytonic.cytosis.config.CytosisConfig.GarageConfig;
import net.cytonic.cytosis.environments.Environment;
import net.cytonic.cytosis.logging.Logger;

@CytosisComponent
public class GarageManager implements Bootstrappable {

    private MinioClient client;
    private final ExecutorService worker;


    public GarageManager() {
        this.worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisGarageWorker")
            .uncaughtExceptionHandler(
                (t, e) -> Logger.error("An uncaught exception occurred on the garage worker thread: " + t.getName(),
                    e)).factory());
    }

    @Override
    public void init() {
        GarageConfig config = Cytosis.get(CytosisConfig.class).garage();

        client = MinioClient
            .builder()
            .endpoint(config.host() + ":" + config.port())
            .credentials(config.username(), config.password())
            .region("garage")
            .build();

        Cytosis.CONTEXT.registerComponent(client);
    }

    @Override
    public void shutdown() {
        try {
            client.close();
        } catch (Exception e) {
            Logger.error("An error occurred whilest shutting down MinIO", e);
        }
    }

    public CompletableFuture<byte[]> downloadObject(String bucket, String object) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        worker.submit(() -> {
            GetObjectArgs request = GetObjectArgs
                .builder()
                .bucket(Cytosis.get(Environment.class).getPrefix().replace('_', '-') + bucket)
                .object(object)
                .build();
            try {
                future.complete(client.getObject(request).readAllBytes());
            } catch (Exception e) {
                Logger.error("An error occurred while trying to download object from bucket: " + bucket, e);
            }
        });
        return future;
    }

    public CompletableFuture<Void> uploadObject(String bucket, String object, byte[] data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            PutObjectArgs request = PutObjectArgs
                .builder()
                .bucket(Cytosis.get(Environment.class).getPrefix().replace('_', '-') + bucket)
                .object(object)
                .data(data, data.length)
                .build();
            try {
                client.putObject(request);
                future.complete(null);
            } catch (Exception e) {
                Logger.error("An error occurred while trying to upload object from bucket: " + bucket, e);
            }
        });
        return future;
    }
}
