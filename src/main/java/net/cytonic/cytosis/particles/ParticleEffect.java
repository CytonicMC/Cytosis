package net.cytonic.cytosis.particles;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

@AllArgsConstructor
public abstract class ParticleEffect {

    @Getter
    final ParticleEffectType type;

    @ApiStatus.Internal
    public abstract void play(ParticleAudience audience);
}
