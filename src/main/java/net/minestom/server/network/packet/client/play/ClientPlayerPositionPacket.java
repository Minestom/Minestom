package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.DOUBLE;

public record ClientPlayerPositionPacket(@NotNull Point position,
                                         boolean onGround) implements ClientPacket {
    public ClientPlayerPositionPacket(@NotNull NetworkBuffer reader) {
        this(new Vec(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE)),
                reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(DOUBLE, position.x());
        writer.write(DOUBLE, position.y());
        writer.write(DOUBLE, position.z());
        writer.write(BOOLEAN, onGround);
    }
}
