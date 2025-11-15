package net.cytonic.cytosis.particles.effects.keyframed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.ApiStatus;

import net.cytonic.cytosis.particles.ParticleEffect;
import net.cytonic.cytosis.particles.util.ParticleSupplier;

public class BezierCurveEffect extends KeyframedEffect {

    private static final EasingFunction DEFAULT_EASING_TYPE = EasingFunction.LINEAR;
    private static final BridgingStrategy DEFAULT_BRIDING_STRATEGY = BridgingStrategy.DEFAULT;
    private static final int DEFAULT_RESOLUTION = 16;
    private static final int DEFAULT_PRECISION = 200;
    private static final int DEFAULT_DURATION = 40; // ticks
    private final Map<Integer, List<ParticleEffect>> keyframeEffects;


    /**
     * Creates a new Bézier curve defined by the supplied points. At least 2 points must be supplied.
     *
     * @param resolution       How many steps to separate this curve into. Higher numbers take longer to compute and
     *                         result in more particles being spawned.
     * @param duration         The number of ticks to play this keyframed effect over. Must be greater than the
     *                         revolution
     * @param precision        the precision of this curve. The higher the number, the more accurate the curve segments
     *                         are. It should be noted that a very high precision will cause performance issues, a range
     *                         of 100-300 is generally recommended.
     * @param type             The type of easing to apply to this keyframable effect
     * @param points           The {@link Pos} objects that define this bézier curve. More info about bézier curves
     *                         <a href="https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Specific_cases">here</a>
     * @param particleSupplier The supplier for the particles used. This is called for every particle drawn, so it
     *                         should be performant.
     * @param strat            The bridging strategy used to connect the points of the curve. For more info, see
     *                         {@link BridgingStrategy}.
     * @throws IllegalArgumentException if {@code points.length} is less than 2
     * @throws IllegalArgumentException if {@code resulution} is less than 2
     * @throws IllegalArgumentException if {@code duration} is less than {@code resolution}
     */
    @ApiStatus.Internal // used with the builder
    public BezierCurveEffect(int resolution, int duration, EasingFunction type, int precision, BridgingStrategy strat,
        ParticleSupplier particleSupplier, Pos... points) {
        if (points.length < 2) {
            throw new IllegalArgumentException("At least 2 points must be supplied.");
        }
        List<Keyframe<?>> keyframes = computeCurve(particleSupplier, resolution, precision, List.of(points), strat);
        keyframeEffects = computeEasing(keyframes, duration, type);
    }

    List<Keyframe<?>> computeCurve(ParticleSupplier supplier, int resolution, int precision, List<Pos> ctrl,
        BridgingStrategy bridge) {
        List<Pos> points = sampleArc(resolution, precision, ctrl);
        List<Keyframe<?>> keyframes = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (i == 0 || i == points.size() - 1) {
                continue; // don't compute bridge
            }
            keyframes.add(new Keyframe<>(bridge.render(supplier, points.get(i - 1), points.get(i))));
        }
        return keyframes;
    }

    private List<Pos> sampleArc(int resolution, int precision, List<Pos> ctrl) {
        if (resolution < 2 || precision < 2) {
            throw new IllegalArgumentException("Resolution and precision must be at least 2.");
        }

        List<Pos> dense = sample(precision, ctrl);
        double[] dist = new double[precision];
        dist[0] = 0;

        for (int i = 1; i < precision; i++) {
            dist[i] = dist[i - 1] + dense.get(i).sub(dense.get(i - 1)).asVec().length();
        }

        double len = dist[precision - 1];
        List<Pos> out = new ArrayList<>(resolution);

        for (int k = 0; k < resolution; k++) {
            double target = len * k / (resolution - 1);

            int idx = Arrays.binarySearch(dist, target);
            if (idx < 0) {
                idx = -idx - 2;
            }
            if (idx < 0) {
                out.add(dense.getFirst());
                continue;
            }
            if (idx >= precision - 1) {
                out.add(dense.getLast());
                continue;
            }

            double segment = dist[idx + 1] - dist[idx];
            double localT = (segment == 0) ? 0 : (target - dist[idx]) / segment;
            out.add(lerp(dense.get(idx), dense.get(idx + 1), localT));
        }
        return out;
    }

    private List<Pos> sample(int resolution, List<Pos> ctrl) {
        if (resolution < 2) {
            throw new IllegalArgumentException("resolution must be greater than 2.");
        }
        List<Pos> pts = new ArrayList<>(resolution);
        double dt = 1.0 / (resolution - 1);
        for (int i = 0; i < resolution; i++) {
            pts.add(evaluate(i * dt, ctrl));
        }
        return pts;
    }

    private Pos lerp(Pos a, Pos b, double t) {
        return a.mul(1 - t).add(b.mul(t));
    }

    private Pos evaluate(double t, List<Pos> ctrl) {
        List<Pos> tmp = new ArrayList<>(ctrl);
        for (int k = ctrl.size() - 1; k > 0; k--) {
            for (int i = 0; i < k; i++) {
                tmp.set(i, lerp(tmp.get(i), tmp.get(i + 1), t));
            }
        }
        return tmp.getFirst();
    }

    @Override
    public Map<Integer, List<ParticleEffect>> getKeyframeEffects() {
        return keyframeEffects;
    }
}
