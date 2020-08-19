package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class ShortArrayData extends DataType<short[]> {

    @Override
    public void encode(BinaryWriter binaryWriter, short[] value) {
        binaryWriter.writeVarInt(value.length);
        for (short val : value) {
            binaryWriter.writeShort(val);
        }
    }

    @Override
    public short[] decode(BinaryReader binaryReader) {
        short[] array = new short[binaryReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = binaryReader.readShort();
        }
        return array;
    }
}
