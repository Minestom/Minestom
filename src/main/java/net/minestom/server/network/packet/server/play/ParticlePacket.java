package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public record ParticlePacket(@NotNull Particle particle, boolean longDistance, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float maxSpeed, int particleCount) implements ServerPacket.Play {
    private ParticlePacket(ParticlePacket copy) {
        this(copy.particle, copy.longDistance, copy.x, copy.y, copy.z, copy.offsetX, copy.offsetY, copy.offsetZ, copy.maxSpeed, copy.particleCount);
    }

    public ParticlePacket(@NotNull NetworkBuffer reader) {
        this(readPacket(reader));
    }

    public ParticlePacket(@NotNull Particle particle, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float maxSpeed, int particleCount) {
        this(particle, false, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, particleCount);
    }

    public ParticlePacket(@NotNull Particle particle, boolean longDistance, @NotNull Point position, @NotNull Point offset, float maxSpeed, int particleCount) {
        this(particle, longDistance, position.x(), position.y(), position.z(), (float)offset.x(), (float)offset.y(), (float)offset.z(), maxSpeed, particleCount);
    }

    public ParticlePacket(@NotNull Particle particle, @NotNull Point position, @NotNull Point offset, float maxSpeed, int particleCount) {
        this(particle, false, position, offset, maxSpeed, particleCount);
    }

    private static ParticlePacket readPacket(NetworkBuffer reader) {
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

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BOOLEAN, longDistance);
        writer.write(DOUBLE, x);
        writer.write(DOUBLE, y);
        writer.write(DOUBLE, z);
        writer.write(FLOAT, offsetX);
        writer.write(FLOAT, offsetY);
        writer.write(FLOAT, offsetZ);
        writer.write(FLOAT, maxSpeed);
        writer.write(INT, particleCount);
        writer.write(VAR_INT, particle.id());
        particle.writeData(writer);
    }

}
