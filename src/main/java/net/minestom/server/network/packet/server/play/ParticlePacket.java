package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public record ParticlePacket(@NotNull Particle particle, boolean longDistance, double x, double y, double z,
                             float offsetX, float offsetY, float offsetZ, float maxSpeed,
                             int particleCount) implements ServerPacket.Play {
    public ParticlePacket(@NotNull Particle particle, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float maxSpeed, int particleCount) {
        this(particle, false, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, particleCount);
    }

    public ParticlePacket(@NotNull Particle particle, boolean longDistance, @NotNull Point position, @NotNull Point offset, float maxSpeed, int particleCount) {
        this(particle, longDistance, position.x(), position.y(), position.z(), (float) offset.x(), (float) offset.y(), (float) offset.z(), maxSpeed, particleCount);
    }

    public ParticlePacket(@NotNull Particle particle, @NotNull Point position, @NotNull Point offset, float maxSpeed, int particleCount) {
        this(particle, false, position, offset, maxSpeed, particleCount);
    }

    public static final NetworkBuffer.Type<ParticlePacket> SERIALIZER = new Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, ParticlePacket value) {
            writer.write(BOOLEAN, value.longDistance);
            writer.write(DOUBLE, value.x);
            writer.write(DOUBLE, value.y);
            writer.write(DOUBLE, value.z);
            writer.write(FLOAT, value.offsetX);
            writer.write(FLOAT, value.offsetY);
            writer.write(FLOAT, value.offsetZ);
            writer.write(FLOAT, value.maxSpeed);
            writer.write(INT, value.particleCount);
            writer.write(VAR_INT, value.particle.id());
            value.particle.writeData(writer);
        }

        @Override
        public ParticlePacket read(@NotNull NetworkBuffer reader) {
            Boolean longDistance = reader.read(BOOLEAN);
            Double x = reader.read(DOUBLE);
            Double y = reader.read(DOUBLE);
            Double z = reader.read(DOUBLE);
            Float offsetX = reader.read(FLOAT);
            Float offsetY = reader.read(FLOAT);
            Float offsetZ = reader.read(FLOAT);
            Float maxSpeed = reader.read(FLOAT);
            Integer particleCount = reader.read(INT);

            Particle particle = Particle.fromId(reader.read(VAR_INT));
            Objects.requireNonNull(particle);

            return new ParticlePacket(particle.readData(reader), longDistance, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, particleCount);
        }
    };
}
