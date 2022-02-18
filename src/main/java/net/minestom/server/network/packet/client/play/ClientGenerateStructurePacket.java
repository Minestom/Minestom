package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientGenerateStructurePacket(@NotNull Point blockPosition,
                                            int level, boolean keepJigsaws) implements ClientPacket {
    public ClientGenerateStructurePacket(BinaryReader reader) {
        this(reader.readBlockPosition(), reader.readVarInt(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeVarInt(level);
        writer.writeBoolean(keepJigsaws);
    }
}
