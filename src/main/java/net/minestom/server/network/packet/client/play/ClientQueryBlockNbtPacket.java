package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

public class ClientQueryBlockNbtPacket extends ClientPlayPacket {

    public int transactionId;
    public Point blockPosition = Vec.ZERO;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.transactionId = reader.readVarInt();
        this.blockPosition = reader.readBlockPosition();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeBlockPosition(blockPosition);
    }
}
