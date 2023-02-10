package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.FLOAT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record UpdateHealthPacket(float health, int food, float foodSaturation) implements ServerPacket {
    public UpdateHealthPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(FLOAT), reader.read(VAR_INT), reader.read(FLOAT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(FLOAT, health);
        writer.write(VAR_INT, food);
        writer.write(FLOAT, foodSaturation);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_HEALTH;
    }
}
