package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.particle.data.ParticleData;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public final class ParticlePacket implements ServerPacket {
    private final int particleId;
    private final boolean longDistance;
    private final double x;
    private final double y;
    private final double z;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final float maxSpeed;
    private final int particleCount;
    private final ParticleData data;

    public ParticlePacket(int particleId, boolean longDistance,
                          double x, double y, double z,
                          float offsetX, float offsetY, float offsetZ,
                          float maxSpeed, int particleCount, ParticleData data) {
        this.particleId = particleId;
        this.longDistance = longDistance;
        this.x = x;
        this.y = y;
        this.z = z;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.maxSpeed = maxSpeed;
        this.particleCount = particleCount;
        this.data = data;
    }

    public ParticlePacket(@NotNull NetworkBuffer reader) {
        this.particleId = reader.read(VAR_INT);
        this.longDistance = reader.read(BOOLEAN);
        this.x = reader.read(DOUBLE);
        this.y = reader.read(DOUBLE);
        this.z = reader.read(DOUBLE);
        this.offsetX = reader.read(FLOAT);
        this.offsetY = reader.read(FLOAT);
        this.offsetZ = reader.read(FLOAT);
        this.maxSpeed = reader.read(FLOAT);
        this.particleCount = reader.read(INT);
        this.data = ParticleData.read(particleId, reader);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
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

    public int particleId() {
        return particleId;
    }

    public boolean longDistance() {
        return longDistance;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float offsetX() {
        return offsetX;
    }

    public float offsetY() {
        return offsetY;
    }

    public float offsetZ() {
        return offsetZ;
    }

    public float maxSpeed() {
        return maxSpeed;
    }

    public int particleCount() {
        return particleCount;
    }

    public ParticleData data() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ParticlePacket) obj;
        return this.particleId == that.particleId &&
                this.longDistance == that.longDistance &&
                Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x) &&
                Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y) &&
                Double.doubleToLongBits(this.z) == Double.doubleToLongBits(that.z) &&
                Float.floatToIntBits(this.offsetX) == Float.floatToIntBits(that.offsetX) &&
                Float.floatToIntBits(this.offsetY) == Float.floatToIntBits(that.offsetY) &&
                Float.floatToIntBits(this.offsetZ) == Float.floatToIntBits(that.offsetZ) &&
                Float.floatToIntBits(this.maxSpeed) == Float.floatToIntBits(that.maxSpeed) &&
                this.particleCount == that.particleCount &&
                Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(particleId, longDistance, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, particleCount, data);
    }

    @Override
    public String toString() {
        return "ParticlePacket[" +
                "particleId=" + particleId + ", " +
                "longDistance=" + longDistance + ", " +
                "x=" + x + ", " +
                "y=" + y + ", " +
                "z=" + z + ", " +
                "offsetX=" + offsetX + ", " +
                "offsetY=" + offsetY + ", " +
                "offsetZ=" + offsetZ + ", " +
                "maxSpeed=" + maxSpeed + ", " +
                "particleCount=" + particleCount + ", " +
                "data=" + data + ']';
    }

}
