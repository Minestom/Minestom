package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientPlayerPositionAndRotationPacket(@NotNull Pos position,
                                                    boolean onGround) implements ClientPacket {
    public ClientPlayerPositionAndRotationPacket(@NotNull NetworkBuffer reader) {
        this(new Pos(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE),
                reader.read(FLOAT), reader.read(FLOAT)), reader.read(BOOLEAN));
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
