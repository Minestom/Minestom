package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class IntegerArrayData extends DataType<int[]> {

    @Override
    public void encode(BinaryWriter binaryWriter, int[] value) {
        binaryWriter.writeVarInt(value.length);
        for (int val : value) {
            binaryWriter.writeInt(val);
        }
    }

    @Override
    public int[] decode(BinaryReader binaryReader) {
        int[] array = new int[binaryReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = binaryReader.readInteger();
        }
        return array;
    }
}
