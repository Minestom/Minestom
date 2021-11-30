package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientPlayerPositionAndRotationPacket(@NotNull Pos position,
                                                    boolean onGround) implements ClientPacket {
    public ClientPlayerPositionAndRotationPacket(BinaryReader reader) {
        this(new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble(),
                reader.readFloat(), reader.readFloat()), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeDouble(position.x());
        writer.writeDouble(position.y());
        writer.writeDouble(position.z());
        writer.writeFloat(position.yaw());
        writer.writeFloat(position.pitch());
        writer.writeBoolean(onGround);
    }
}
