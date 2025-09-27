package net.cytonic.cytosis.particles.effects.keyframed;

import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.ApiStatus;

import net.cytonic.cytosis.particles.effects.fixed.LineEffect;
import net.cytonic.cytosis.particles.effects.fixed.SingleParticleEffect;
import net.cytonic.cytosis.particles.effects.fixed.StaticEffect;
import net.cytonic.cytosis.particles.util.ParticleSupplier;

/**
 * The interface describing how geometrical effects "bridge" the points of the shape. By default, {@code line(10)} is
 * used.
 */
public interface BridgingStrategy {

    /**
     * The default implementation with a low-density line (5 particles per block).
     */
    BridgingStrategy DEFAULT = line(5);

    //TODO: Make a non-deterministic impl for more randomness

    /**
     * Bridges between two points by placing a particle between the two positions
     *
     * @return The midpoint strategy
     */
    static BridgingStrategy midpoint() {
        return new Midpoint();
    }

    /**
     * Places a particle at the second of the two positions in question
     *
     * @return The endpoint strategy
     */
    static BridgingStrategy end() {
        return new End();
    }

    /**
     * Places a particle at the first of the two positions in question
     *
     * @return The startpoint strategy
     */
    static BridgingStrategy start() {
        return new Start();
    }

    /**
     * Places a series of particles in a line between the two points
     *
     * @param density The number of particles per block of distance between the two "bridged" particles
     * @return The endpoint strategy
     */
    static BridgingStrategy line(int density) {
        return new Line(density);
    }

    /**
     * Does nothing
     *
     * @return the NOOP strategy
     */
    static BridgingStrategy none() {
        return new None();
    }

    @ApiStatus.Internal
    StaticEffect render(ParticleSupplier supplier, Point start, Point end);

    @ApiStatus.Internal
    record Line(int density) implements BridgingStrategy {

        @Override
        public StaticEffect render(ParticleSupplier supplier, Point start, Point end) {
            return new LineEffect(supplier, start, end, density);
        }
    }

    @ApiStatus.Internal
    record Start() implements BridgingStrategy {

        @Override
        public StaticEffect render(ParticleSupplier supplier, Point start, Point end) {
            return new SingleParticleEffect(supplier, start);
        }
    }

    @ApiStatus.Internal
    record End() implements BridgingStrategy {

        @Override
        public StaticEffect render(ParticleSupplier supplier, Point start, Point end) {
            return new SingleParticleEffect(supplier, end);
        }
    }

    @ApiStatus.Internal
    record Midpoint() implements BridgingStrategy {

        @Override
        public StaticEffect render(ParticleSupplier supplier, Point start, Point end) {
            double x;
            double y;
            double z;
            x = (start.x() + end.x()) / 2;
            y = (start.y() + end.y()) / 2;
            z = (start.z() + end.z()) / 2;
            return new SingleParticleEffect(supplier, new Pos(x, y, z));
        }
    }

    @ApiStatus.Internal
    record None() implements BridgingStrategy {

        @Override
        public StaticEffect render(ParticleSupplier supplier, Point start, Point end) {
            return new StaticEffect() {
                @Override
                public void play(PacketGroupingAudience audience) {

                }
            }; // an empty effect
        }
    }
}
