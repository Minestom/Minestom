package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.FLOAT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record HitAnimationPacket(int entityId, float yaw) implements ServerPacket {

    public HitAnimationPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(FLOAT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(FLOAT, yaw);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.HIT_ANIMATION;
    }
}
