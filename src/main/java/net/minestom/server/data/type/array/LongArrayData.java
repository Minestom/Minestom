package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class LongArrayData extends DataType<long[]> {

    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull long[] value) {
        writer.writeVarInt(value.length);
        for (long val : value) {
            writer.writeLong(val);
        }
    }

    @NotNull
    @Override
    public long[] decode(@NotNull BinaryReader reader) {
        long[] array = new long[reader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readLong();
        }
        return array;
    }
}
