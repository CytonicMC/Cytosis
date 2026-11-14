package net.cytonic.cytosis.particles.events;

import java.util.List;
import java.util.UUID;

import lombok.Data;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.particles.ParticleEffect;
import net.cytonic.cytosis.particles.util.ParticleData;

@Data
public class ParticleSpawnEvent implements CancellableEvent {

    private final Pos position;
    @Nullable
    private final List<UUID> recipients;
    private final ParticleData particleData;
    private final ParticleEffect effect;
    private boolean cancelled = false;

}
