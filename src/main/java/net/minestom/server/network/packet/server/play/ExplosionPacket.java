package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ExplosionPacket(float x, float y, float z, float radius, byte @NotNull [] records,
                              float playerMotionX, float playerMotionY, float playerMotionZ) implements ServerPacket {
    public ExplosionPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(FLOAT), reader.read(FLOAT), reader.read(FLOAT),
                reader.read(FLOAT), reader.readBytes(reader.read(VAR_INT) * 3),
                reader.read(FLOAT), reader.read(FLOAT), reader.read(FLOAT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(FLOAT, x);
        writer.write(FLOAT, y);
        writer.write(FLOAT, z);
        writer.write(FLOAT, radius);
        writer.write(VAR_INT, records.length / 3); // each record is 3 bytes long
        writer.write(RAW_BYTES, records);
        writer.write(FLOAT, playerMotionX);
        writer.write(FLOAT, playerMotionY);
        writer.write(FLOAT, playerMotionZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.EXPLOSION;
    }
}
