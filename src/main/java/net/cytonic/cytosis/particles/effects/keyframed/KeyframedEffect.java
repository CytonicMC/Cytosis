package net.cytonic.cytosis.particles.effects.keyframed;

import net.cytonic.cytosis.particles.Keyframeable;
import net.cytonic.cytosis.particles.ParticleEffect;
import net.cytonic.cytosis.particles.ParticleEffectType;
import net.minestom.server.adventure.audience.PacketGroupingAudience;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class KeyframedEffect extends ParticleEffect implements Keyframeable {
    public KeyframedEffect() {
        super(ParticleEffectType.KEYFRAMED);
    }

    /**
     * Computes the easing on these keyframes. Easing is the spacing of the keyframes over time.
     * {@link EasingFunction#LINEAR} easing means each keyframe is spaced evenly. While an easing type like
     * {@link EasingFunction#EXPONENTIAL} means the keyframes get exponentially closer to eachother in time.
     *
     * @param keyframes      the keyframes to ease
     * @param duration       the duration of the total effect. Must be longer than {@code effects.size()}
     * @param easingFunction The function to use when easing the keyframes.
     * @return the map of effects timed according to the specified easing function.
     * @throws IllegalArgumentException if {@code effects.size()} is greater than duration
     */
    protected Map<Integer, List<ParticleEffect>> computeEasing(List<Keyframe<?>> keyframes, int duration, EasingFunction easingFunction) {
        if (keyframes.isEmpty()) {
            return new LinkedHashMap<>();
        }

        Map<Integer, List<ParticleEffect>> result = new LinkedHashMap<>();

        // Handle single keyframe case
        if (keyframes.size() == 1) {
            addEffectToTick(result, 0, keyframes.getFirst().effect());
            return result;
        }

        // Place each keyframe at its eased position in the timeline
        for (int i = 0; i < keyframes.size(); i++) {
            double linearProgress = i / (double) (keyframes.size() - 1);
            double easedProgress = applyEasing(linearProgress, easingFunction);
            int tick = (int) Math.round(easedProgress * (duration - 1));

            addEffectToTick(result, tick, keyframes.get(i).effect());
        }

        return result;
    }

    private double applyEasing(double t, EasingFunction easingFunction) {
        return switch (easingFunction) {
            case LINEAR -> t;
            case QUADRATIC -> t * t;
            case CUBIC -> t * t * t;
            case SINE -> 1 - Math.cos(t * Math.PI / 2);
            case CIRCULAR -> 1 - Math.sqrt(1 - t * t);
            case EXPONENTIAL -> t == 0 ? 0 : Math.pow(2, 10 * (t - 1));
        };
    }

    private void addEffectToTick(Map<Integer, List<ParticleEffect>> map, int tick, ParticleEffect effect) {
        map.computeIfAbsent(tick, k -> new ArrayList<>()).add(effect);
    }

    @Override
    public void play(PacketGroupingAudience audience) {
        throw new UnsupportedOperationException("Keyframed effects cannot be played directly. Use ParticleEngine#playKeyframed(PacketGroupingAudience, KeyframedEffect) instead.");
    }
}
