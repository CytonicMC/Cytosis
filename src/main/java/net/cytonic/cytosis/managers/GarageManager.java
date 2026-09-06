package net.cytonic.cytosis.managers;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
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
        return CompletableFuture.supplyAsync(() -> {
            GetObjectArgs request = GetObjectArgs
                    .builder()
                    .bucket(Cytosis.get(Environment.class).getPrefix().replace('_', '-') + bucket)
                    .object(object)
                    .build();

            try (GetObjectResponse rs = client.getObject(request)) {
                return rs.readAllBytes();
            } catch (MinioException | IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public CompletableFuture<Void> uploadObject(String bucket, String object, byte[] data) {
        return CompletableFuture.supplyAsync(() -> {
            PutObjectArgs request = PutObjectArgs
                    .builder()
                    .bucket(Cytosis.get(Environment.class).getPrefix().replace('_', '-') + bucket)
                    .object(object)
                    .data(data, data.length)
                    .build();
            try {
                client.putObject(request);
                return null;
            } catch (MinioException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
