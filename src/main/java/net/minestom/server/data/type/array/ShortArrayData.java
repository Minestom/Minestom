package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class ShortArrayData extends DataType<short[]> {

    @Override
    public void encode(BinaryWriter writer, short[] value) {
        writer.writeVarInt(value.length);
        for (short val : value) {
            writer.writeShort(val);
        }
    }

    @Override
    public short[] decode(BinaryReader reader) {
        short[] array = new short[reader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readShort();
        }
        return array;
    }
}
