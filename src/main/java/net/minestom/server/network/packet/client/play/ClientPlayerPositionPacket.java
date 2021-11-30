package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientPlayerPositionPacket(@NotNull Point position,
                                         boolean onGround) implements ClientPacket {
    public ClientPlayerPositionPacket(BinaryReader reader) {
        this(new Vec(reader.readDouble(), reader.readDouble(), reader.readDouble()),
                reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeDouble(position.x());
        writer.writeDouble(position.y());
        writer.writeDouble(position.z());
        writer.writeBoolean(onGround);
    }
}
