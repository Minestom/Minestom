package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientQueryBlockNbtPacket(int transactionId, @NotNull Point blockPosition) implements ClientPacket {
    public ClientQueryBlockNbtPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(BLOCK_POSITION));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, transactionId);
        writer.write(BLOCK_POSITION, blockPosition);
    }
}
