package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class IntegerArrayData extends DataType<int[]> {

    @Override
    public void encode(BinaryWriter writer, int[] value) {
        writer.writeVarInt(value.length);
        for (int val : value) {
            writer.writeInt(val);
        }
    }

    @Override
    public int[] decode(BinaryReader reader) {
        int[] array = new int[reader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readInteger();
        }
        return array;
    }
}
