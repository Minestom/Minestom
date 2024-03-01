package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.data.ParticleData;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record ParticlePacket(int particleId, boolean longDistance, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float maxSpeed, int particleCount, @Nullable ParticleData data) implements ServerPacket {
    private ParticlePacket(ParticlePacket copy) {
        this(copy.particleId, copy.longDistance, copy.x, copy.y, copy.z, copy.offsetX, copy.offsetY, copy.offsetZ, copy.maxSpeed, copy.particleCount, copy.data);
    }

    public ParticlePacket(@NotNull NetworkBuffer reader) {
        this(readPacket(reader));
    }

    public ParticlePacket(@NotNull Particle particle, boolean longDistance, double x, double y, double z, float offsetX, float offsetY, float offsetZ, int maxSpeed, int particleCount) {
        this(particle.id(), longDistance, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, particleCount, particle.data());
    }

    public ParticlePacket(@NotNull Particle particle, double x, double y, double z, float offsetX, float offsetY, float offsetZ, int maxSpeed, int particleCount) {
        this(particle.id(), false, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, particleCount, particle.data());
    }

    private static ParticlePacket readPacket(NetworkBuffer reader) {
        int particleId = reader.read(VAR_INT);
        Boolean longDistance = reader.read(BOOLEAN);
        Double x = reader.read(DOUBLE);
        Double y = reader.read(DOUBLE);
        Double z = reader.read(DOUBLE);
        Float offsetX = reader.read(FLOAT);
        Float offsetY = reader.read(FLOAT);
        Float offsetZ = reader.read(FLOAT);
        Float maxSpeed = reader.read(FLOAT);
        Integer particleCount = reader.read(INT);
        ParticleData data = ParticleData.read(particleId, reader);

        return new ParticlePacket(particleId, longDistance, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, particleCount, data);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        Check.stateCondition(data != null && !data.validate(particleId), "Particle data {0} is not valid for this particle type {1}", data, Particle.fromId(particleId));
        Check.stateCondition(data == null && ParticleData.requiresData(particleId), "Particle data is required for this particle type {0}", Particle.fromId(particleId));

        writer.write(VAR_INT, particleId);
        writer.write(BOOLEAN, longDistance);
        writer.write(DOUBLE, x);
        writer.write(DOUBLE, y);
        writer.write(DOUBLE, z);
        writer.write(FLOAT, offsetX);
        writer.write(FLOAT, offsetY);
        writer.write(FLOAT, offsetZ);
        writer.write(FLOAT, maxSpeed);
        writer.write(INT, particleCount);

        if (data != null) data.write(writer);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.PARTICLE;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
