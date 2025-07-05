package net.cytonic.cytosis.particles.effects.fixed;

import net.cytonic.cytosis.particles.util.ParticleSupplier;
import net.cytonic.cytosis.utils.Utils;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

import java.util.List;

public class LineEffect extends StaticEffect {
    Pos pos1, pos2;
    int density;
    ParticleSupplier supplier;
    List<Pos> positions;

    public LineEffect(ParticleSupplier supplier, Pos pos1, Pos pos2, int density) {
        this.supplier = supplier;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.density = density;
        this.positions = computePositions();
    }

    private List<Pos> computePositions() {
        double dist = pos1.distance(pos2);
        int steps = (int) (density * dist);
        double increment = dist / steps;
        List<Pos> positions = Utils.list(pos1);
        Vec normalized = pos2.sub(pos1).asVec().normalize();
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
