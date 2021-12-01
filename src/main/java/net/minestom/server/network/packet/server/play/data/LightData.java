package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.List;

public record LightData(boolean trustEdges,
                        @NotNull BitSet skyMask, @NotNull BitSet blockMask,
                        @NotNull BitSet emptySkyMask, @NotNull BitSet emptyBlockMask,
                        @NotNull List<byte[]> skyLight, @NotNull List<byte[]> blockLight) implements Writeable {
    public LightData(BinaryReader reader) {
        this(reader.readBoolean(),
                BitSet.valueOf(reader.readLongArray()), BitSet.valueOf(reader.readLongArray()),
                BitSet.valueOf(reader.readLongArray()), BitSet.valueOf(reader.readLongArray()),
                reader.readVarIntList(r -> r.readBytes(r.readVarInt())), reader.readVarIntList(r -> r.readBytes(r.readVarInt())));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBoolean(trustEdges);

        writer.writeLongArray(skyMask.toLongArray());
        writer.writeLongArray(blockMask.toLongArray());

        writer.writeLongArray(emptySkyMask.toLongArray());
        writer.writeLongArray(emptyBlockMask.toLongArray());

        writer.writeVarIntList(skyLight, BinaryWriter::writeByteArray);
        writer.writeVarIntList(blockLight, BinaryWriter::writeByteArray);
    }
}
