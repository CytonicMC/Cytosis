package net.cytonic.cytosis.particles.effects.keyframed;

import org.jetbrains.annotations.ApiStatus;

import net.cytonic.cytosis.particles.ParticleEffect;

@ApiStatus.Internal
public record Keyframe<T extends ParticleEffect>(T effect) {

    public void play() {
        //        ParticleEngine.playEffect(effect);
    }
}
