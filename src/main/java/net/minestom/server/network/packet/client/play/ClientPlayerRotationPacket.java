package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record ClientPlayerRotationPacket(float yaw, float pitch, boolean onGround) implements ClientPacket {
    public ClientPlayerRotationPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(FLOAT), reader.read(FLOAT), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(FLOAT, yaw);
        writer.write(FLOAT, pitch);
        writer.write(BOOLEAN, onGround);
    }
}
