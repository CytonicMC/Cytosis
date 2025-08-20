package net.cytonic.cytosis.particles.util;

@FunctionalInterface
public interface ParticleSupplier {
    static ParticleSupplier random(ParticleData... data) {
        if (data.length == 0) {
            throw new IllegalArgumentException("At least one particle data is required");
        }
        return () -> data[(int) (Math.random() * data.length)];
    }

    ParticleData get();
}
