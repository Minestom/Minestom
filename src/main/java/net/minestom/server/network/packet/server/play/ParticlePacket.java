package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public record ParticlePacket(Particle particle, boolean overrideLimiter, boolean longDistance,
                             Point origin, Point offset, float maxSpeed, int particleCount) implements ServerPacket.Play {
    public ParticlePacket(Particle particle, boolean overrideLimiter, boolean longDistance, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float maxSpeed, int particleCount) {
        this(particle, overrideLimiter, longDistance, new Vec(x, y, z), new Vec(offsetX, offsetY, offsetZ), maxSpeed, particleCount);
    }

    public ParticlePacket(Particle particle, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float maxSpeed, int particleCount) {
        this(particle, false, false, new Vec(x, y, z), new Vec(offsetX, offsetY, offsetZ), maxSpeed, particleCount);
    }

    public ParticlePacket(Particle particle, Point origin, float offsetX, float offsetY, float offsetZ, float maxSpeed, int particleCount) {
        this(particle, false, false, origin, new Vec(offsetX, offsetY, offsetZ), maxSpeed, particleCount);
    }

    public ParticlePacket(Particle particle, Point origin, Point offset, float maxSpeed, int particleCount) {
        this(particle, false, false, origin, offset, maxSpeed, particleCount);
    }

    private ParticlePacket(boolean overrideLimiter, boolean longDistance, Point origin,
                           Point offset, float maxSpeed,
                           int particleCount, Particle particle) {
        this(particle, overrideLimiter, longDistance, origin, offset, maxSpeed, particleCount);
    }

    public ParticlePacket {
        Objects.requireNonNull(particle, "particle");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(offset, "offset");
    }

    public static final NetworkBuffer.Type<ParticlePacket> SERIALIZER = NetworkBufferTemplate.template(
            BOOLEAN, ParticlePacket::overrideLimiter,
            BOOLEAN, ParticlePacket::longDistance,
            VECTOR3D, ParticlePacket::origin,
            VECTOR3, ParticlePacket::offset,
            FLOAT, ParticlePacket::maxSpeed,
            INT, ParticlePacket::particleCount,
            Particle.NETWORK_TYPE, ParticlePacket::particle,
            ParticlePacket::new
    );
}
