package net.minestom.server.particle;

import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.data.ParticleData;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

/**
 * Small utils class to create particle packet
 */
public class ParticleCreator {

    public static ParticlePacket createParticlePacket(@NotNull ParticleData particleData, boolean distance,
                                                      double x, double y, double z,
                                                      float offsetX, float offsetY, float offsetZ,
                                                      float speed, int count) {
        ParticlePacket particlePacket = new ParticlePacket();
        particlePacket.particleId = particleData.getParticle().getId();
        particlePacket.longDistance = distance;

        particlePacket.x = x;
        particlePacket.y = y;
        particlePacket.z = z;

        particlePacket.offsetX = offsetX;
        particlePacket.offsetY = offsetY;
        particlePacket.offsetZ = offsetZ;

        particlePacket.speed = speed;
        particlePacket.particleCount = count;

        BinaryWriter writer = new BinaryWriter();
        particleData.write(writer);
        particlePacket.data = writer.toByteArray();

        return particlePacket;
    }

    public static ParticlePacket createParticlePacket(@NotNull ParticleData particleData,
                                                      double x, double y, double z,
                                                      float offsetX, float offsetY, float offsetZ,
                                                      int count) {
        return createParticlePacket(particleData, false,
                x, y, z,
                offsetX, offsetY, offsetZ,
                0, count);
    }
}
