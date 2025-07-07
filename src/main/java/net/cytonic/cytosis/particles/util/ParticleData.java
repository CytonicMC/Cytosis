package net.cytonic.cytosis.particles.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public class ParticleData {

    Particle particle;
    boolean overrideLimiter, longDistance;
    Point offset;
    float maxSpeed;
    int particleCount;

    private ParticleData(@NotNull Particle particle, boolean overrideLimiter, boolean longDistance, Point offset, float maxSpeed, int particleCount) {
        this.particle = particle;
        this.overrideLimiter = overrideLimiter;
        this.longDistance = longDistance;
        this.offset = offset;
        this.maxSpeed = maxSpeed;
        this.particleCount = particleCount;
    }

    public static ParticleData simple(Particle p) {
        return new ParticleData(p, false, false, Pos.ZERO, 0, 1);
    }

    public static ParticleData simple(Particle p, int particleCount) {
        return new ParticleData(p, false, false, Pos.ZERO, 0, particleCount);
    }

    public static ParticleData of(Particle p, Point offset, float maxSpeed, int particleCount) {
        return new ParticleData(p, false, false, offset, maxSpeed, particleCount);
    }

    public static ParticleData of(Particle p, boolean overrideLimiter, boolean longDistance, Point offset, float maxSpeed, int particleCount) {
        return new ParticleData(p, overrideLimiter, longDistance, offset, maxSpeed, particleCount);
    }

    public ParticlePacket getPacket(Point pos) {
        return new ParticlePacket(particle, overrideLimiter, longDistance, pos, offset, maxSpeed, particleCount);
    }
}
