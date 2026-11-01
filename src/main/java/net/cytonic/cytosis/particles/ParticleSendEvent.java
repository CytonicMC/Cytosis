package net.cytonic.cytosis.particles;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.particles.util.ParticleData;
import net.cytonic.cytosis.player.CytosisPlayer;

@Getter
@Setter
public class ParticleSendEvent implements CancellableEvent, InstanceEvent {

    /**
     * Null means it was sent to the entire instance
     */
    @Nullable
    private final Set<UUID> recipients;
    private final ParticleData data;
    private final Pos pos;
    private final Instance instance;
    private boolean cancelled;

    public ParticleSendEvent(ParticleData data, Instance instance, Pos pos, UUID... recipients) {
        this.recipients = Set.of(recipients);
        this.pos = pos;
        this.data = data;
        this.instance = instance;
    }

    @Internal
    public ParticleSendEvent(ParticlePacket packet, Instance i, @Nullable Set<CytosisPlayer> recipients) {
        if (recipients != null) {
            this.recipients = recipients.stream().map(Entity::getUuid).collect(Collectors.toSet());
        } else this.recipients = null;
        this.pos = new Pos(packet.x(), packet.y(), packet.z());
        this.data = ParticleData.fromPacket(packet);
        this.instance = i;
    }
}
