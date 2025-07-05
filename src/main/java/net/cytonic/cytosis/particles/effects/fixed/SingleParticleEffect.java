package net.cytonic.cytosis.particles.effects.fixed;

import lombok.AllArgsConstructor;
import net.cytonic.cytosis.particles.util.ParticleSupplier;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Pos;

@AllArgsConstructor
public class SingleParticleEffect extends StaticEffect {
    private final ParticleSupplier particle;
    private final Pos pos;

    @Override
    public void play(PacketGroupingAudience audience) {
        audience.sendGroupedPacket(particle.get().getPacket(pos));
    }
}
