package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record ProjectilePowerPacket(
        int entityId, double accelerationPower
) implements ServerPacket.Play {

    public ProjectilePowerPacket(@NotNull NetworkBuffer buffer) {
        this(buffer.read(NetworkBuffer.VAR_INT), buffer.read(NetworkBuffer.DOUBLE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, entityId);
        writer.write(NetworkBuffer.DOUBLE, accelerationPower);
    }

}
