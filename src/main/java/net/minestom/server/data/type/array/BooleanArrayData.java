package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class BooleanArrayData extends DataType<boolean[]> {

    @Override
    public void encode(BinaryWriter writer, boolean[] value) {
        writer.writeVarInt(value.length);
        for (boolean val : value) {
            writer.writeBoolean(val);
        }
    }

    @Override
    public boolean[] decode(BinaryReader reader) {
        boolean[] array = new boolean[reader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readBoolean();
        }
        return array;
    }
}
