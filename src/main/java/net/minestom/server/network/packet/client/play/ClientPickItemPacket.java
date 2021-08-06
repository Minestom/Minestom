package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientPickItemPacket extends ClientPlayPacket {

    public int slotToUse;

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        this.slotToUse = reader.readVarInt();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(slotToUse);
    }
}
