package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public record ParticlePacket(Particle particle, boolean overrideLimiter, boolean longDistance, double x, double y, double z,
                             float offsetX, float offsetY, float offsetZ, float maxSpeed,
                             int particleCount) implements ServerPacket.Play {
    public ParticlePacket(Particle particle, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float maxSpeed, int particleCount) {
        this(particle, false, false, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, particleCount);
    }

    public ParticlePacket(Particle particle, boolean overrideLimiter, boolean longDistance, Point position, Point offset, float maxSpeed, int particleCount) {
        this(particle, overrideLimiter, longDistance, position.x(), position.y(), position.z(), (float) offset.x(), (float) offset.y(), (float) offset.z(), maxSpeed, particleCount);
    }

    public ParticlePacket(Particle particle, Point position, Point offset, float maxSpeed, int particleCount) {
        this(particle, false, false, position, offset, maxSpeed, particleCount);
    }

    public static final NetworkBuffer.Type<ParticlePacket> SERIALIZER = new Type<>() {
        @Override
        public void write(NetworkBuffer buffer, ParticlePacket value) {
            buffer.write(BOOLEAN, value.overrideLimiter);
            buffer.write(BOOLEAN, value.longDistance);
            buffer.write(DOUBLE, value.x);
            buffer.write(DOUBLE, value.y);
            buffer.write(DOUBLE, value.z);
            buffer.write(FLOAT, value.offsetX);
            buffer.write(FLOAT, value.offsetY);
            buffer.write(FLOAT, value.offsetZ);
            buffer.write(FLOAT, value.maxSpeed);
            buffer.write(INT, value.particleCount);
            buffer.write(VAR_INT, value.particle.id());
            value.particle.writeData(buffer);
        }

        @Override
        public ParticlePacket read(NetworkBuffer buffer) {
            Boolean overrideLimiter = buffer.read(BOOLEAN);
            Boolean longDistance = buffer.read(BOOLEAN);
            Double x = buffer.read(DOUBLE);
            Double y = buffer.read(DOUBLE);
            Double z = buffer.read(DOUBLE);
            Float offsetX = buffer.read(FLOAT);
            Float offsetY = buffer.read(FLOAT);
            Float offsetZ = buffer.read(FLOAT);
            Float maxSpeed = buffer.read(FLOAT);
            Integer particleCount = buffer.read(INT);

            Particle particle = Particle.fromId(buffer.read(VAR_INT));
            Objects.requireNonNull(particle);

            return new ParticlePacket(particle.readData(buffer), overrideLimiter, longDistance, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, particleCount);
        }
    };
}
