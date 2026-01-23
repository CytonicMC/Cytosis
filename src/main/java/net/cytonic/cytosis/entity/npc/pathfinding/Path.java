package net.cytonic.cytosis.entity.npc.pathfinding;

import java.util.List;
import java.util.function.Consumer;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;

public interface Path {

    List<Pos> getNodes();

    /**
     * Returns the blocks/tick this path should be traveled at
     *
     * @return
     */
    double getSpeed();

    Consumer<Entity> onFinish();
}
