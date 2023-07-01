package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record ClientSteerBoatPacket(boolean leftPaddleTurning, boolean rightPaddleTurning) implements ClientPacket {
    public ClientSteerBoatPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BOOLEAN), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BOOLEAN, leftPaddleTurning);
        writer.write(BOOLEAN, rightPaddleTurning);
    }
}
