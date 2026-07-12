package net.cytonic.cytosis.particles;

import java.util.Collection;
import java.util.Set;

import lombok.AllArgsConstructor;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import net.cytonic.cytosis.player.CytosisPlayer;

@AllArgsConstructor
public class ParticleAudience implements PacketGroupingAudience {

    @Nullable
    private final Set<CytosisPlayer> players;
    Instance instance;

    /**
     * A single player audience
     */
    public static ParticleAudience singleton(CytosisPlayer player) {
        return new ParticleAudience(Set.of(player), player.getInstance());
    }

    /**
     * A static particle audience of the given players
     */
    public static ParticleAudience of(CytosisPlayer... players) {
        if (players.length < 1)
            throw new IllegalArgumentException("There must be at least one player in a ParticleAudience");
        return new ParticleAudience(Set.of(players), players[0].getInstance());
    }

    /**
     * A dynamic audience which send packets to all players of an instance. Each time a packet is sent, the player
     * recipient set is refreshed.
     */
    public static ParticleAudience of(Instance instance) {
        return new ParticleAudience(null, instance);
    }

    @Override
    @NotNull
    public Collection<? extends Player> getPlayers() {
        if (players == null) return instance.getPlayers();
        return players;
    }

    @Override
    public void sendGroupedPacket(@NonNull ServerPacket packet) {
        if (!(packet instanceof ParticlePacket p))
            throw new IllegalArgumentException("Particle audiences should only be used for Particle Packets.");
        ParticleSendEvent e = new ParticleSendEvent(p, players);
        EventDispatcher.callCancellable(e, () -> PacketGroupingAudience.super.sendGroupedPacket(packet));
    }
}
