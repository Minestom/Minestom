package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class BooleanArrayData extends DataType<boolean[]> {

    @Override
    public void encode(BinaryWriter binaryWriter, boolean[] value) {
        binaryWriter.writeVarInt(value.length);
        for (boolean val : value) {
            binaryWriter.writeBoolean(val);
        }
    }

    @Override
    public boolean[] decode(BinaryReader binaryReader) {
        boolean[] array = new boolean[binaryReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = binaryReader.readBoolean();
        }
        return array;
    }
}
