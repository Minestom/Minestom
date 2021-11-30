package net.minestom.server.particle;

import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Small utils class to create particle packet
 */
public class ParticleCreator {

    public static ParticlePacket createParticlePacket(Particle particleType, boolean distance,
                                                      double x, double y, double z,
                                                      float offsetX, float offsetY, float offsetZ,
                                                      float particleData, int count, @Nullable Consumer<BinaryWriter> dataWriter) {
        byte[] data;
        if (dataWriter != null) {
            BinaryWriter writer = new BinaryWriter();
            dataWriter.accept(writer);
            data = writer.toByteArray();
        } else {
            data = new byte[0];
        }
        return new ParticlePacket(particleType.id(), distance, x, y, z, offsetX, offsetY, offsetZ, particleData, count, data);
    }

    public static ParticlePacket createParticlePacket(Particle particleType,
                                                      double x, double y, double z,
                                                      float offsetX, float offsetY, float offsetZ,
                                                      int count) {
        return createParticlePacket(particleType, true, x, y, z,
                offsetX, offsetY, offsetZ, 0, count, null);
    }
}
