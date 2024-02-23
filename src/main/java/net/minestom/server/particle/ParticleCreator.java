package net.minestom.server.particle;

import net.minestom.server.network.packet.server.play.ParticlePacket;

/**
 * Small utils class to create particle packet
 */
public class ParticleCreator {

    public static ParticlePacket createParticlePacket(Particle particleType, boolean distance,
                                                      double x, double y, double z,
                                                      float offsetX, float offsetY, float offsetZ,
                                                      float particleData, int count) {
        return new ParticlePacket(particleType.id(), distance, x, y, z, offsetX, offsetY, offsetZ, particleData, count, particleType.data());
    }

    public static ParticlePacket createParticlePacket(Particle particleType,
                                                      double x, double y, double z,
                                                      float offsetX, float offsetY, float offsetZ,
                                                      int count) {
        return createParticlePacket(particleType, true, x, y, z,
                offsetX, offsetY, offsetZ, 0, count);
    }
}
