package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientQueryBlockNbtPacket(int transactionId, @NotNull Point blockPosition) implements ClientPacket {
    public ClientQueryBlockNbtPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readBlockPosition());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeBlockPosition(blockPosition);
    }
}
