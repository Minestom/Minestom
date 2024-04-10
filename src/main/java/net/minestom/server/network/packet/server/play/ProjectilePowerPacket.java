package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

public record ProjectilePowerPacket(
        int entityId, @NotNull Point power
) implements ServerPacket.Play {

    public ProjectilePowerPacket(@NotNull NetworkBuffer buffer) {
        this(buffer.read(NetworkBuffer.VAR_INT), buffer.read(NetworkBuffer.VECTOR3D));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, entityId);
        writer.write(NetworkBuffer.VECTOR3D, power);
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.PROJECTILE_POWER;
    }

}
