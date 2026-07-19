package net.cytonic.cytosis.particles.effects.looping;

import java.util.function.Supplier;

import net.minestom.server.coordinate.Point;

import net.cytonic.cytosis.particles.ParticleAudience;
import net.cytonic.cytosis.particles.ParticleEngine;
import net.cytonic.cytosis.particles.effects.fixed.SingleParticleEffect;
import net.cytonic.cytosis.particles.util.ParticleSupplier;

/**
 * Emits particles from a given origin, or a supplier of an origin
 */
public class Emitter extends LoopingEffect {

    private final double frequency;
    private final ParticleSupplier supplier;
    private double accumulator = 0;
    private long currentTick;

    public Emitter(Supplier<Point> originSupplier, double frequency, ParticleSupplier supplier) {
        super(originSupplier);
        this.frequency = frequency;
        this.supplier = supplier;
    }

    public Emitter(Point origin, double frequency, ParticleSupplier supplier) {
        super(() -> origin);
        this.frequency = frequency;
        this.supplier = supplier;
    }

    @Override
    public void playNextTick(ParticleAudience audience, ParticleEngine engine) {
        currentTick++;
        if (frequency < 1) {
            int interval = (int) Math.round(1 / frequency);
            if (currentTick % interval == 0) emitParticle(audience, engine);
        } else {
            accumulator += frequency;
            while (accumulator >= 1) {
                emitParticle(audience, engine);
                accumulator -= 1;
            }
        }
    }

    private void emitParticle(ParticleAudience audience, ParticleEngine engine) {
        engine.playStatic(new SingleParticleEffect(supplier, getPosSupplier().get()), audience);
    }
}
