package net.cytonic.cytosis.particles.effects.keyframed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.cytonic.cytosis.particles.ParticleEffect;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@AllArgsConstructor
public class Keyframe<T extends ParticleEffect> {
    @Getter
    final T effect;

    public void play() {
//        ParticleEngine.playEffect(effect);
    }
}
