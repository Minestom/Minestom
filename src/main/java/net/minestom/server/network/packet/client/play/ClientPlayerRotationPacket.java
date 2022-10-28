package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record ClientPlayerRotationPacket(float yaw, float pitch, boolean onGround) implements ClientPacket {
    public ClientPlayerRotationPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(FLOAT), reader.read(FLOAT), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeFloat(yaw);
        writer.writeFloat(pitch);
        writer.writeBoolean(onGround);
    }
}
