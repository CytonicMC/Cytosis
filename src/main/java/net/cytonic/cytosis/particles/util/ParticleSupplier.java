package net.cytonic.cytosis.particles.util;

@FunctionalInterface
public interface ParticleSupplier {
    ParticleData get();

    static ParticleSupplier random(ParticleData... data) {
        return () -> data[(int) (Math.random() * data.length)];
    }
}
