package fr.themode.minestom.particle;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.play.ParticlePacket;

import java.util.function.Consumer;

public class ParticleCreator {

    public static ParticlePacket createParticlePacket(Particle particle, boolean distance,
                                                      double x, double y, double z,
                                                      float offsetX, float offsetY, float offsetZ,
                                                      float particleData, int count, Consumer<PacketWriter> dataWriter) {
        ParticlePacket particlePacket = new ParticlePacket();
        particlePacket.particleId = particle.getId();
        particlePacket.longDistance = distance;

        particlePacket.x = x;
        particlePacket.y = y;
        particlePacket.z = z;

        particlePacket.offsetX = offsetX;
        particlePacket.offsetY = offsetY;
        particlePacket.offsetZ = offsetZ;

        particlePacket.particleData = particleData;
        particlePacket.particleCount = count;
        particlePacket.dataConsumer = dataWriter;

        return particlePacket;
    }

    public static ParticlePacket createParticlePacket(Particle particle,
                                                      double x, double y, double z,
                                                      float offsetX, float offsetY, float offsetZ,
                                                      int count) {
        return createParticlePacket(particle, false,
                x, y, z,
                offsetX, offsetY, offsetZ,
                0, count, null);
    }

}
