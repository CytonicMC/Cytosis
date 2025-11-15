package net.cytonic.cytosis.particles.effects.fixed;

import java.util.List;

import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

import net.cytonic.cytosis.particles.util.ParticleSupplier;
import net.cytonic.cytosis.utils.Utils;

public class LineEffect extends StaticEffect {

    Point pos1;
    Point pos2;
    double density;
    ParticleSupplier supplier;
    List<Point> positions;

    public LineEffect(ParticleSupplier supplier, Point pos1, Point pos2, double density) {
        this.supplier = supplier;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.density = density;
        this.positions = computePositions();
    }

    private List<Point> computePositions() {
        double dist = pos1.distance(pos2);
        int steps = (int) (density * dist);
        double increment = dist / steps;
        List<Point> positions = Utils.list(pos1);
        Vec normalized = Vec.fromPoint(pos2.sub(pos1)).normalize();
        for (int i = 1; i < steps; i++) {
            double t = i * increment;
            positions.add(pos1.add(normalized.mul(t)));
        }
        return positions;
    }

    @Override
    public void play(PacketGroupingAudience audience) {
        positions.forEach(p -> audience.sendGroupedPacket(supplier.get().getPacket(p)));
    }
}
