package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class LongArrayData extends DataType<long[]> {

    @Override
    public void encode(BinaryWriter binaryWriter, long[] value) {
        binaryWriter.writeVarInt(value.length);
        for (long val : value) {
            binaryWriter.writeLong(val);
        }
    }

    @Override
    public long[] decode(BinaryReader binaryReader) {
        long[] array = new long[binaryReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = binaryReader.readLong();
        }
        return array;
    }
}
