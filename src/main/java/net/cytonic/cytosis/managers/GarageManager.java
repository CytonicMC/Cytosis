package net.cytonic.cytosis.managers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import io.minio.errors.MinioException;
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
        return downloadObjectAsStream(bucket, object).thenApplyAsync(rs -> {
            try {
                return rs.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Returns a completable future of an input stream. The stream must be closed to free up resources after reading.
     */
    public CompletableFuture<InputStream> downloadObjectAsStream(String bucket, String object) {
        return CompletableFuture.supplyAsync(() -> {
            GetObjectArgs request = GetObjectArgs
                    .builder()
                    .bucket(Cytosis.get(Environment.class).getPrefix().replace('_', '-') + bucket)
                    .object(object)
                    .build();

            try {
                return client.getObject(request);
            } catch (MinioException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public CompletableFuture<Void> uploadObject(String bucket, String object, byte[] data) {
        return uploadObject(
                bucket,
                object,
                new ByteArrayInputStream(data),
                data.length
        );
    }

    /**
     * Uploads an object from a stream.
     *
     * @param bucket     bucket name without the environment prefix
     * @param object     object key
     * @param data       input stream containing the object data
     * @param objectSize object size in bytes, or -1 if unknown
     */
    public CompletableFuture<Void> uploadObject(
            String bucket,
            String object,
            InputStream data,
            long objectSize
    ) {
        return CompletableFuture.runAsync(() -> {
            String prefixedBucket = Cytosis.get(Environment.class)
                    .getPrefix()
                    .replace('_', '-') + bucket;

            PutObjectArgs request = PutObjectArgs
                    .builder()
                    .bucket(prefixedBucket)
                    .object(object)
                    .stream(
                            data,
                            objectSize,
                            10 * 1024 * 1024L // 10 MiB multipart part size
                    )
                    .build();

            try (InputStream input = data) {
                client.putObject(request);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to upload object '" + object + "' to bucket '" + prefixedBucket + "'",
                        e
                );
            }
        });
    }
}
