package net.cytonic.cytosis.particles.effects.fixed;

import lombok.AllArgsConstructor;
import net.minestom.server.coordinate.Point;

import net.cytonic.cytosis.particles.ParticleAudience;
import net.cytonic.cytosis.particles.util.ParticleSupplier;

@AllArgsConstructor
public class SingleParticleEffect extends StaticEffect {

    private final ParticleSupplier particle;
    private final Point pos;

    @Override
    public void play(ParticleAudience audience) {
        audience.sendGroupedPacket(particle.get().getPacket(pos));
    }
}
