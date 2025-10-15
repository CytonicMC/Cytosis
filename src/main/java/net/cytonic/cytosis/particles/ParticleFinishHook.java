package net.cytonic.cytosis.particles;

@FunctionalInterface
public interface ParticleFinishHook {

    void onFinish(ParticleEffect effect);
}
