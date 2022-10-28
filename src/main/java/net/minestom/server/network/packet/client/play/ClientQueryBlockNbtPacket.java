package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientQueryBlockNbtPacket(int transactionId, @NotNull Point blockPosition) implements ClientPacket {
    public ClientQueryBlockNbtPacket(NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.VAR_INT), reader.read(NetworkBuffer.BLOCK_POSITION));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeBlockPosition(blockPosition);
    }
}
