package net.cytonic.cytosis.particles;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.cytosis.particles.effects.fixed.StaticEffect;
import net.cytonic.cytosis.particles.effects.keyframed.KeyframedEffect;
import net.cytonic.cytosis.particles.effects.looping.LoopingEffect;

/**
 * The class that handles all the interactions with the particle api. It's the primary entrypoint.
 */
@AllArgsConstructor
public class ParticleEngine {

    public static final Tag<ParticleEngine> TAG = Tag.Transient("cytosis:particle-engine");
    public static final int THREAD_POOL_SIZE = 1;
    private final Instance instance;
    private final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    private ParticleAudience getAudience() {
        return ParticleAudience.of(instance);
    }

    /**
     * Plays this keyframed effect for every Cytosis player on this instance. For more fined grained control over whom
     * the effect is played for, use {@link #playKeyframed(ParticleAudience, KeyframedEffect)}
     *
     * @param effect the keyframed effect to play
     */
    public void playKeyframed(KeyframedEffect effect) {
        playKeyframed(getAudience(), effect);
    }

    /**
     * Plays this keyframed effect for every player in the instance. To play this for everyone, use
     * {@link #playKeyframed(KeyframedEffect)}
     *
     * @param effect the keyframed effect to play
     */
    public void playKeyframed(ParticleAudience audience, KeyframedEffect effect) {
        playKeyframed(audience, effect, 0);
    }

    /**
     * Plays this keyframed effect for every player in the instance. To play this for everyone, use
     * {@link #playKeyframed(KeyframedEffect)}
     *
     * @param effect the keyframed effect to play
     * @param delay  the delay in milliseconds before the effect starts playing
     */
    public void playKeyframed(ParticleAudience audience, KeyframedEffect effect, int delay) {
        if (delay == 0) {
            playKeyFramedInternal(audience, effect);
            return;
        }
        SCHEDULER.schedule(() -> playKeyFramedInternal(audience, effect), delay, TimeUnit.MILLISECONDS);
    }

    @Internal
    private void playKeyFramedInternal(ParticleAudience audience, KeyframedEffect effect) {
        effect.getKeyframeEffects().forEach((time, effectsToPlay) -> {
            if (time <= 0) {
                effectsToPlay.forEach(eff -> eff.play(audience));
                return;
            }
            SCHEDULER.schedule(() -> effectsToPlay.forEach(eff -> eff.play(audience)), time, TimeUnit.MILLISECONDS);
        });
    }

    public ScheduledFuture<?> playLooping(LoopingEffect effect, int period) {
        return playLooping(effect, period, getAudience());
    }

    public ScheduledFuture<?> playLooping(LoopingEffect effect, int period, ParticleAudience audience) {
        return playLooping(effect, period, audience, 0);
    }

    public ScheduledFuture<?> playLooping(LoopingEffect effect, int period, ParticleAudience audience,
        int delay) {
        return SCHEDULER.scheduleAtFixedRate(() -> effect.playNextTick(audience, this), delay, period,
            TimeUnit.MILLISECONDS);
    }

    public void playStatic(StaticEffect effect) {
        playStatic(effect, getAudience());
    }

    public void playStatic(StaticEffect effect, ParticleAudience audience) {
        playStatic(effect, audience, 0);
    }

    public void playStatic(StaticEffect effect, ParticleAudience audience, int delay) {
        if (delay == 0) {
            effect.play(audience);
            return;
        }
        SCHEDULER.schedule(() -> effect.play(audience), delay, TimeUnit.MILLISECONDS);
    }
}
