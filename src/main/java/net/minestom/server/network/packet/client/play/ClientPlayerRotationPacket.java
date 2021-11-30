package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientPlayerRotationPacket(float yaw, float pitch, boolean onGround) implements ClientPacket {
    public ClientPlayerRotationPacket(BinaryReader reader) {
        this(reader.readFloat(), reader.readFloat(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeFloat(yaw);
        writer.writeFloat(pitch);
        writer.writeBoolean(onGround);
    }
}
