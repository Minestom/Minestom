package net.minestom.server.particle;

import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Small utils class to create particle packet
 */
public class ParticleCreator {

    /**
     * @deprecated Use {@link #createParticlePacket(ParticleOption, boolean, double, double, double, float, float, float, float, int)} instead
     */

    @Deprecated(forRemoval = true)
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


    public static ParticlePacket createParticlePacket(@NotNull ParticleOption option, boolean distance,
                                                      double x, double y, double z,
                                                      float offsetX, float offsetY, float offsetZ,
                                                      float particleData, int count) {

        return new ParticlePacket(option.type().id(), distance, x, y, z, offsetX, offsetY, offsetZ, particleData, count, BinaryWriter.makeArray(option::write));
    }
}