package net.cytonic.cytosis.particles;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.particles.effects.fixed.StaticEffect;
import net.cytonic.cytosis.particles.effects.keyframed.KeyframedEffect;
import net.cytonic.cytosis.particles.effects.looping.LoopingEffect;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;

/**
 * The class that handles all the interactions with the particle api. It's the primary entrypoint.
 */
public class ParticleEngine {
    private static final SchedulerManager SCHEDULER = MinecraftServer.getSchedulerManager();

    /**
     * Plays this keyframed effect for every Cytosis player. For more fined grained control over who the effect
     * is played for, use {@link #playKeyframed(PacketGroupingAudience, KeyframedEffect)}
     *
     * @param effect the keyframed effect to play
     */
    public static void playKeyframed(KeyframedEffect effect) {
        PacketGroupingAudience audience = PacketGroupingAudience.of(new ArrayList<>(Cytosis.getOnlinePlayers()));
        playKeyframed(audience, effect);
    }

    /**
     * Plays this keyframed effect for every Cytosis player. To play this for everyone,
     * use {@link #playKeyframed(KeyframedEffect)}
     *
     * @param effect the keyframed effect to play
     */
    public static void playKeyframed(PacketGroupingAudience audience, KeyframedEffect effect) {
        effect.getKeyframeEffects().forEach((time, effectsToPlay) -> {
            if (time <= 0) {
                effectsToPlay.forEach(eff -> eff.play(audience));
                return;
            }
            SCHEDULER.buildTask(() -> effectsToPlay.forEach(eff -> eff.play(audience))).delay(TaskSchedule.tick(time)).schedule();
        });
    }

    public static Task playLooping(LoopingEffect effect, TaskSchedule period) {
        return playLooping(effect, period, PacketGroupingAudience.of(new ArrayList<>(Cytosis.getOnlinePlayers())));
    }

    public static Task playLooping(LoopingEffect effect, TaskSchedule period, PacketGroupingAudience audience) {
        return SCHEDULER.buildTask(() -> effect.playNextTick(audience)).repeat(period).schedule();
    }

    public static void playStatic(StaticEffect effect) {
        playStatic(effect, PacketGroupingAudience.of(new ArrayList<>(Cytosis.getOnlinePlayers())));
    }

    public static void playStatic(StaticEffect effect, PacketGroupingAudience audience) {
        effect.play(audience);
    }
}
