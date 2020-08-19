package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class CharacterArrayData extends DataType<char[]> {
    @Override
    public void encode(BinaryWriter writer, char[] value) {
        writer.writeVarInt(value.length);
        for (char val : value) {
            writer.writeChar(val);
        }
    }

    @Override
    public char[] decode(BinaryReader reader) {
        char[] array = new char[reader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readChar();
        }
        return array;
    }
}
