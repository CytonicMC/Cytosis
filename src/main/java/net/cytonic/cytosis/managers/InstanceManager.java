package net.cytonic.cytosis.managers;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.DatabaseManager;
import net.cytonic.cytosis.utils.polar.PolarExtension;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.utils.polar.PolarExtension;

@NoArgsConstructor
public class InstanceManager implements Bootstrappable {
    private DatabaseManager databaseManager;

    @Override
    public void init() {
        this.databaseManager = Cytosis.CONTEXT.getComponent(DatabaseManager.class);
    }

    public CompletableFuture<Instance> loadDatabaseWorld(String databaseName, @Nullable UUID uuid) {
        CompletableFuture<Instance> future = new CompletableFuture<>();
        databaseManager.getMysqlDatabase().getWorld(databaseName).whenComplete((world, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }

            InstanceContainer container = new InstanceContainer(uuid == null ? UUID.randomUUID() : uuid,
                DimensionType.OVERWORLD);
            container.setChunkLoader(new PolarLoader(world).setWorldAccess(new PolarExtension()));
            Cytosis.CONTEXT.getComponent(net.minestom.server.instance.InstanceManager.class).registerInstance(container);
            future.complete(container);
        });
        return future;
    }

    public CompletableFuture<Instance> loadDatabaseWorld(String databaseName) {
        CompletableFuture<Instance> future = new CompletableFuture<>();
        databaseManager.getMysqlDatabase().getWorld(databaseName).whenComplete((world, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }

            InstanceContainer container = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
            container.setChunkLoader(new PolarLoader(world).setWorldAccess(new PolarExtension()));
            Cytosis.CONTEXT.getComponent(net.minestom.server.instance.InstanceManager.class).registerInstance(container);
            future.complete(container);
        });
        return future;
    }

    public CompletableFuture<String> getExtraData(String worldName, String worldType) {
        CompletableFuture<String> future = new CompletableFuture<>();
        databaseManager.getMysqlDatabase().getWorldExtraData(worldName, worldType)
            .whenComplete((extraData, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                } else {
                    future.complete(extraData);
                }
            });
        return future;
    }
}
