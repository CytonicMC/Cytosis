package net.cytonic.cytosis.npcs.pathfinding;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;

import java.util.List;
import java.util.function.Consumer;

public record LoopingPath(double walkingSpeed, Pos... points) implements Path {


    @Override
    public List<Pos> getNodes() {
        return List.of(points);
    }

    @Override
    public double getSpeed() {
        return walkingSpeed;
    }

    @Override
    public Consumer<Entity> onFinish() {
        return entity -> {
            // yay! Start again!
        };
    }
}
