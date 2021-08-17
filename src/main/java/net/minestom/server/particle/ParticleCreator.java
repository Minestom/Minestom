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
        ParticlePacket particlePacket = new ParticlePacket();
        particlePacket.particleId = particleType.id();
        particlePacket.longDistance = distance;

        particlePacket.x = x;
        particlePacket.y = y;
        particlePacket.z = z;

        particlePacket.offsetX = offsetX;
        particlePacket.offsetY = offsetY;
        particlePacket.offsetZ = offsetZ;

        particlePacket.particleData = particleData;
        particlePacket.particleCount = count;

        if (dataWriter != null) {
            BinaryWriter writer = new BinaryWriter();
            dataWriter.accept(writer);
            particlePacket.data = writer.toByteArray();
        } else {
            particlePacket.data = new byte[0];
        }

        return particlePacket;
    }

    public static ParticlePacket createParticlePacket(Particle particleType,
                                                      double x, double y, double z,
                                                      float offsetX, float offsetY, float offsetZ,
                                                      int count) {
        return createParticlePacket(particleType, true,
                x, y, z,
                offsetX, offsetY, offsetZ,
                0, count, null);
    }

}
