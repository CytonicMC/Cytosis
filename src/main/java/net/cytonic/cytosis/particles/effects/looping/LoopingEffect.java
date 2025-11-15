package net.cytonic.cytosis.particles.effects.looping;

import java.util.function.Supplier;

import lombok.Getter;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;

import net.cytonic.cytosis.particles.ParticleEffect;
import net.cytonic.cytosis.particles.ParticleEffectType;

@Getter
public abstract class LoopingEffect extends ParticleEffect {

    private final Supplier<Point> posSupplier;

    public LoopingEffect(Supplier<Point> originSupplier) {
        super(ParticleEffectType.LOOPING);
        this.posSupplier = originSupplier;
    }

    @Override
    public void play(PacketGroupingAudience audience) {
        throw new UnsupportedOperationException(
            "Looping effects cannot be played directly. Use ParticleEngine#playLooping(LoopingEffect, TaskSchedule,"
                + " PacketGroupingAudience) instead.");
    }

    public abstract void playNextTick(PacketGroupingAudience audience);
}
