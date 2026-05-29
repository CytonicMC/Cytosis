package net.cytonic.cytosis.managers;

import java.util.concurrent.CompletableFuture;

import lombok.NoArgsConstructor;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.kyori.adventure.key.Key;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;

@NoArgsConstructor
@CytosisComponent(dependsOn = {net.minestom.server.instance.InstanceManager.class})
public class InstanceManager {

    public CompletableFuture<PolarWorld> loadWorld(Key key) {
        CompletableFuture<PolarWorld> future = new CompletableFuture<>();
        Cytosis.get(MinioManager.class).downloadObject(
            "cytonic-worlds",
            "/" + key.namespace() + "/" + key.value() + ".polar"
        ).whenComplete((data, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            future.complete(PolarReader.read(data));
        });
        return future;
    }

    public CompletableFuture<Void> saveWorld(Key key, PolarWorld world) {
        return Cytosis.get(MinioManager.class).uploadObject(
            "cytonic-worlds",
            "/" + key.namespace() + "/" + key.value() + ".polar",
            PolarWriter.write(world)
        );
    }
}
