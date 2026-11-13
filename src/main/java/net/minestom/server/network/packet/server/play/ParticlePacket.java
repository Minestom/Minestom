package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.DOUBLE;
import static net.minestom.server.network.NetworkBuffer.FLOAT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

/**
 * Spawns particles around a position on the client.
 *
 * @param particle        the particle and its data
 * @param overrideLimiter whether the client skips its particle count limit
 * @param longDistance    whether the client shows the particles beyond its usual particle range
 * @param x               the x coordinate of the center
 * @param y               the y coordinate of the center
 * @param z               the z coordinate of the center
 * @param offsetX         the x spread applied to each particle
 * @param offsetY         the y spread applied to each particle
 * @param offsetZ         the z spread applied to each particle
 * @param maxSpeedX       the x speed bound, used as the exact x velocity when {@code particleCount} is zero
 * @param maxSpeedY       the y speed bound, used as the exact y velocity when {@code particleCount} is zero
 * @param maxSpeedZ       the z speed bound, used as the exact z velocity when {@code particleCount} is zero
 * @param particleCount   the number of particles, zero spawns one particle with the speed as its velocity
 * @param randomization   how the client randomizes the spread and speed of each particle
 */
public record ParticlePacket(Particle particle, boolean overrideLimiter, boolean longDistance, double x, double y, double z,
                             float offsetX, float offsetY, float offsetZ, float maxSpeedX, float maxSpeedY, float maxSpeedZ,
                             int particleCount, Randomization randomization) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ParticlePacket> SERIALIZER = NetworkBufferTemplate.template(
            Particle.NETWORK_TYPE, ParticlePacket::particle,
            BOOLEAN, ParticlePacket::overrideLimiter,
            BOOLEAN, ParticlePacket::longDistance,
            DOUBLE, ParticlePacket::x,
            DOUBLE, ParticlePacket::y,
            DOUBLE, ParticlePacket::z,
            FLOAT, ParticlePacket::offsetX,
            FLOAT, ParticlePacket::offsetY,
            FLOAT, ParticlePacket::offsetZ,
            FLOAT, ParticlePacket::maxSpeedX,
            FLOAT, ParticlePacket::maxSpeedY,
            FLOAT, ParticlePacket::maxSpeedZ,
            VAR_INT, ParticlePacket::particleCount,
            Randomization.NETWORK_TYPE, ParticlePacket::randomization,
            ParticlePacket::new);

    public ParticlePacket(Particle particle, boolean overrideLimiter, boolean longDistance, double x, double y, double z,
                          float offsetX, float offsetY, float offsetZ, float maxSpeed, int particleCount) {
        this(particle, overrideLimiter, longDistance, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, maxSpeed, maxSpeed, particleCount, Randomization.DEFAULT);
    }

    public ParticlePacket(Particle particle, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float maxSpeed, int particleCount) {
        this(particle, false, false, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, particleCount);
    }

    public ParticlePacket(Particle particle, boolean overrideLimiter, boolean longDistance, Point position, Point offset, float maxSpeed, int particleCount) {
        this(particle, overrideLimiter, longDistance, position.x(), position.y(), position.z(), (float) offset.x(), (float) offset.y(), (float) offset.z(), maxSpeed, particleCount);
    }

    public ParticlePacket(Particle particle, Point position, Point offset, float maxSpeed, int particleCount) {
        this(particle, false, false, position, offset, maxSpeed, particleCount);
    }

    /**
     * How the client randomizes the spread and speed of each spawned particle.
     */
    public enum Randomization {
        DEFAULT,
        ALTERNATIVE,
        ALTERNATIVE_WITH_SPEED;

        public static final NetworkBuffer.Type<Randomization> NETWORK_TYPE = NetworkBuffer.Enum(Randomization.class);
    }
}
