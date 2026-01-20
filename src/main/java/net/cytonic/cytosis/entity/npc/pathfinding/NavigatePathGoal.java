package net.cytonic.cytosis.entity.npc.pathfinding;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.tag.Tag;

import net.cytonic.cytosis.logging.Logger;

public class NavigatePathGoal extends GoalSelector {

    private static final Tag<Boolean> PATHING_TAG = Tag.Boolean("pathing").defaultValue(true);
    private final EntityCreature entity;
    private final Path path;
    private int index;

    public NavigatePathGoal(EntityCreature creature, Path path) {
        super(creature);
        this.entity = creature;
        this.path = path;
    }

    @Override
    public boolean shouldStart() {
        return entity.getTag(PATHING_TAG);
    }

    @Override
    public void start() {
        entity.setTag(PATHING_TAG, !entity.getNavigator().setPathTo(currentNode(), 1, () -> {
            Logger.debug("Should move again!");
            entity.setTag(PATHING_TAG, true);
        }));

        index++;
        if (index >= path.getNodes().size()) {
            index = 0;
            path.onFinish().accept(entity);
        }
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public boolean shouldEnd() {
        return true;
    }

    @Override
    public void end() {

    }

    private Pos currentNode() {
        return path.getNodes().get(index);
    }
}
