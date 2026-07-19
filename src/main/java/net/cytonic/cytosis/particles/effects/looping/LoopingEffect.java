package net.cytonic.cytosis.particles.effects.looping;

import java.util.function.Supplier;

import lombok.Getter;
import net.minestom.server.coordinate.Point;

import net.cytonic.cytosis.particles.ParticleAudience;
import net.cytonic.cytosis.particles.ParticleEffect;
import net.cytonic.cytosis.particles.ParticleEffectType;
import net.cytonic.cytosis.particles.ParticleEngine;

@Getter
public abstract class LoopingEffect extends ParticleEffect {

    private final Supplier<Point> posSupplier;

    public LoopingEffect(Supplier<Point> originSupplier) {
        super(ParticleEffectType.LOOPING);
        this.posSupplier = originSupplier;
    }

    @Override
    public void play(ParticleAudience audience) {
        throw new UnsupportedOperationException(
            "Looping effects cannot be played directly. Use ParticleEngine#playLooping(LoopingEffect, int,"
                + " PacketGroupingAudience) instead.");
    }

    public abstract void playNextTick(ParticleAudience audience, ParticleEngine ignored);
}
