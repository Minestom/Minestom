package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class ParticlePacket implements ServerPacket {

    public int particleId;
    public boolean longDistance;
    public double x, y, z;
    public float offsetX, offsetY, offsetZ;
    public float particleData;
    public int particleCount;

    public Consumer<BinaryWriter> dataConsumer;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeInt(particleId);
        writer.writeBoolean(longDistance);
        writer.writeDouble(x);
        writer.writeDouble(y);
        writer.writeDouble(z);
        writer.writeFloat(offsetX);
        writer.writeFloat(offsetY);
        writer.writeFloat(offsetZ);
        writer.writeFloat(particleData);
        writer.writeInt(particleCount);

        if (dataConsumer != null) {
            dataConsumer.accept(writer);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PARTICLE;
    }
}
