package net.cytonic.cytosis.particles;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import org.jetbrains.annotations.ApiStatus;

@AllArgsConstructor
public abstract class ParticleEffect {

    @Getter
    final ParticleEffectType type;

    @ApiStatus.Internal
    public abstract void play(PacketGroupingAudience audience);
}
