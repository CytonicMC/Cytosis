package net.cytonic.cytosis.display;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;

public class DisplayLine extends Entity {

    public DisplayLine(Point p1, Point p2) {
        super(EntityType.BLOCK_DISPLAY);
        double distance = p1.distance(p2);

        editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setHasNoGravity(true);
            meta.setScale(new Vec(0.0625, 0.0625, distance));
//            meta.setLeftRotation();
        });
    }
}
