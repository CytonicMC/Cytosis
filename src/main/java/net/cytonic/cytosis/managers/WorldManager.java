package net.cytonic.cytosis.managers;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import lombok.NoArgsConstructor;
import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.InstanceManager;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;

@NoArgsConstructor
@CytosisComponent(dependsOn = {InstanceManager.class})
public class WorldManager {

    public CompletableFuture<PolarLoader> loadWorld(Key key) {
        CompletableFuture<PolarLoader> future = new CompletableFuture<>();
        Cytosis.get(GarageManager.class).downloadObjectAsStream(
            "cytonic-worlds",
            "/" + key.namespace() + "/" + key.value() + ".polar"
        ).whenComplete((data, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            try {
                future.complete(new PolarLoader(data));
            } catch (IOException e) {
                future.completeExceptionally(e);
                throw new RuntimeException(e);
            }
        });
        return future;
    }

    public CompletableFuture<Void> saveWorld(Key key, PolarWorld world) {
        return Cytosis.get(GarageManager.class).uploadObject(
            "cytonic-worlds",
            "/" + key.namespace() + "/" + key.value() + ".polar",
            PolarWriter.write(world)
        );
    }
}
