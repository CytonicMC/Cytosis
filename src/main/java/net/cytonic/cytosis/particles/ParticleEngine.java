package net.cytonic.cytosis.particles;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.minestom.server.adventure.audience.PacketGroupingAudience;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.particles.effects.fixed.StaticEffect;
import net.cytonic.cytosis.particles.effects.keyframed.KeyframedEffect;
import net.cytonic.cytosis.particles.effects.looping.LoopingEffect;

/**
 * The class that handles all the interactions with the particle api. It's the primary entrypoint.
 */
public class ParticleEngine {

    public static final int THREAD_POOL_SIZE = 1;
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    /**
     * Plays this keyframed effect for every Cytosis player. For more fined grained control over who the effect is
     * played for, use {@link #playKeyframed(PacketGroupingAudience, KeyframedEffect)}
     *
     * @param effect the keyframed effect to play
     */
    public static void playKeyframed(KeyframedEffect effect) {
        PacketGroupingAudience audience = PacketGroupingAudience.of(new ArrayList<>(Cytosis.getOnlinePlayers()));
        playKeyframed(audience, effect);
    }

    /**
     * Plays this keyframed effect for every player in the audience. To play this for everyone, use
     * {@link #playKeyframed(KeyframedEffect)}
     *
     * @param effect the keyframed effect to play
     */
    public static void playKeyframed(PacketGroupingAudience audience, KeyframedEffect effect) {
        playKeyframed(audience, effect, 0);
    }

    /**
     * Plays this keyframed effect for every player in the audience. To play this for everyone, use
     * {@link #playKeyframed(KeyframedEffect)}
     *
     * @param effect the keyframed effect to play
     * @param delay  the delay in milliseconds before the effect starts playing
     */
    public static void playKeyframed(PacketGroupingAudience audience, KeyframedEffect effect, int delay) {
        if (delay == 0) {
            playKeyFramedInternal(audience, effect);
            return;
        }
        SCHEDULER.schedule(() -> playKeyFramedInternal(audience, effect), delay, TimeUnit.MILLISECONDS);
    }

    @Internal
    private static void playKeyFramedInternal(PacketGroupingAudience audience, KeyframedEffect effect) {
        effect.getKeyframeEffects().forEach((time, effectsToPlay) -> {
            if (time <= 0) {
                effectsToPlay.forEach(eff -> eff.play(audience));
                return;
            }
            SCHEDULER.schedule(() -> effectsToPlay.forEach(eff -> eff.play(audience)), time, TimeUnit.MILLISECONDS);
        });
    }

    public static ScheduledFuture<?> playLooping(LoopingEffect effect, int period) {
        return playLooping(effect, period, PacketGroupingAudience.of(new ArrayList<>(Cytosis.getOnlinePlayers())));
    }

    public static ScheduledFuture<?> playLooping(LoopingEffect effect, int period, PacketGroupingAudience audience) {
        return playLooping(effect, period, audience, 0);
    }

    public static ScheduledFuture<?> playLooping(LoopingEffect effect, int period, PacketGroupingAudience audience,
        int delay) {
        return SCHEDULER.scheduleAtFixedRate(() -> effect.playNextTick(audience), delay, period, TimeUnit.MILLISECONDS);
    }

    public static void playStatic(StaticEffect effect) {
        playStatic(effect, PacketGroupingAudience.of(new ArrayList<>(Cytosis.getOnlinePlayers())));
    }

    public static void playStatic(StaticEffect effect, PacketGroupingAudience audience) {
        playStatic(effect, audience, 0);
    }

    public static void playStatic(StaticEffect effect, PacketGroupingAudience audience, int delay) {
        if (delay == 0) {
            effect.play(audience);
            return;
        }
        SCHEDULER.schedule(() -> effect.play(audience), delay, TimeUnit.MILLISECONDS);
    }
}
