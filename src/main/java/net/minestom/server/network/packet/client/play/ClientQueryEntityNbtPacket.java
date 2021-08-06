package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientQueryEntityNbtPacket extends ClientPlayPacket {

    public int transactionId;
    public int entityId;

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        this.transactionId = reader.readVarInt();
        this.entityId = reader.readVarInt();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeVarInt(entityId);
    }
}
