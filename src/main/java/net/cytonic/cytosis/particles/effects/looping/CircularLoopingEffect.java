package net.cytonic.cytosis.particles.effects.looping;

import net.cytonic.cytosis.particles.effects.keyframed.BridgingStrategy;
import net.cytonic.cytosis.particles.util.ParticleSupplier;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;
import java.util.function.Supplier;

public class CircularLoopingEffect extends LoopingEffect {
    private final double INCREMENT;
    private final int resolution;
    private final ParticleSupplier particleSupplier;
    private final Function<Double, Double> radiusFunc;
    private final Function<Double, Point> offsetFunc;
    private final Phase phase;
    private final BridgingStrategy bridge;
    private final Point[] cachedPositions;
    private final double phaseShift;
    private double radius;
    private Point offset;
    private int currentTick = 0;
    private double currentAngle = 0;
    private Point last;

    /**
     * Creates a looping effect which results in a circle
     *
     * @param posSupplier      The entity this effect revolves around. todo: Make it also work with a pos
     * @param resolution       The number of segments in the circle should be broken into. 16 typically works well
     * @param phase            The phase this effect should play in. For more info, see the {@link Phase} class
     * @param offsetFunc       The function to supply the offset of the particle. The double parameter is an angle from
     *                         0 to 2π, allowing for dynamic offsets based on the current angle of the rotation. There
     *                         is a helper method, {@link CircularLoopingEffect#getPhasedOffset(double, Phase)} to get
     *                         its offset relative to a phase.
     * @param radiusFunc       The function called to supply the radius of the circular loop. The double parameter is
     *                         an angle from 0 to 2π, which allows for the dynamic radius based on the current angle.
     *                         Note: It is the RADIUS, so half the width of the circle.
     * @param particleSupplier The supplier for the particles used. This is called for every particle drawn, so it
     *                         should be performant.
     * @param bridge           The strategy used to connect the points of the circular path. For more details about the
     *                         available strategies, read the documentation on the {@link BridgingStrategy} class.
     * @param phaseOffset      The amount to shift the phase to this effect. Use the {@link Angles} class for convenient
     *                         conversions. Note: The unit on this is radians, not degrees!
     * @see Phase
     */
    @ApiStatus.Internal
    public CircularLoopingEffect(Supplier<Point> posSupplier, int resolution, Phase phase, Function<Double, Point> offsetFunc,
                                 Function<Double, Double> radiusFunc, ParticleSupplier particleSupplier, BridgingStrategy bridge,
                                 double phaseOffset) {
        super(posSupplier);
        this.resolution = resolution;
        this.particleSupplier = particleSupplier;
        this.offsetFunc = offsetFunc;
        this.radiusFunc = radiusFunc;
        this.INCREMENT = 2 * Math.PI / resolution;
        this.phase = phase;
        this.bridge = bridge;
        this.phaseShift = phaseOffset;
        this.offset = offsetFunc.apply(currentAngle);
        this.radius = radiusFunc.apply(currentAngle);
        cachedPositions = new Pos[resolution];
        cachePositions();
    }

    /**
     * Gets the offset associated with the angle and phase
     *
     * @param angle The current angle to get the offset from.
     * @param phase The phase to get the offset relative to
     * @return the offset associated with the given angle and phase, always between -1 and 1.
     */
    @SuppressWarnings("unused")
    public static double getPhasedOffset(double angle, Phase phase) {
        return getPhasedRadius(angle, phase, 0);
    }

    /**
     * Gets the offset associated with the angle and phase
     *
     * @param angle      The current angle to get the offset from.
     * @param phase      The phase to get the offset relative to
     * @param phaseShift The shift to apply to the phase.
     * @return the offset associated with the given angle and phase, always between -1 and 1.
     */
    public static double getPhasedRadius(double angle, Phase phase, double phaseShift) {
        return switch (phase) {
            case ONE -> Math.sin(angle + phaseShift);
            case TWO -> Math.cos(angle + phaseShift);
            case THREE -> -Math.sin(angle + phaseShift);
            case FOUR -> -Math.cos(angle + phaseShift);
        };
    }

    @Override
    @ApiStatus.Internal
    public void playNextTick(PacketGroupingAudience audience) {
        if (currentTick >= resolution) {
            currentAngle = 0;
            currentTick = 0;
        }

        currentTick++;
        currentAngle += INCREMENT;

        this.radius = radiusFunc.apply(currentAngle);
        this.offset = offsetFunc.apply(currentAngle);

        Point center = getPosSupplier().get().add(offset);
        Point loc = getPos(center);
        if (last != null) {
            bridge.render(particleSupplier, loc, last).play(audience);
        }
        last = loc;
    }

    private Point getPos(Point center) {
        return center.add(cachedPositions[currentTick - 1]);
    }

    private Point calculatePos(double angle) {
        return switch (phase) {
            case ONE -> new Pos(Math.sin(angle + phaseShift) * radius, 0, Math.cos(angle + phaseShift) * radius);
            case TWO -> new Pos(Math.cos(angle + phaseShift) * radius, 0, Math.sin(angle + phaseShift) * radius);
            case THREE -> new Pos(-Math.sin(angle + phaseShift) * radius, 0, -Math.cos(angle + phaseShift) * radius);
            case FOUR -> new Pos(-Math.cos(angle + phaseShift) * radius, 0, -Math.sin(angle + phaseShift) * radius);
        };
    }

    private void cachePositions() {
        for (int i = 0; i < resolution; i++) {
            cachedPositions[i] = calculatePos(i * INCREMENT);
        }
    }
}
