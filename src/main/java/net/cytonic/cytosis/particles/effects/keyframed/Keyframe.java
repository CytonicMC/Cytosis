package net.cytonic.cytosis.particles.effects.keyframed;

import net.cytonic.cytosis.particles.ParticleEffect;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record Keyframe<T extends ParticleEffect>(T effect) {
    public void play() {
//        ParticleEngine.playEffect(effect);
    }
}
