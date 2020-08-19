package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class CharacterArrayData extends DataType<char[]> {
    @Override
    public void encode(BinaryWriter binaryWriter, char[] value) {
        binaryWriter.writeVarInt(value.length);
        for (char val : value) {
            binaryWriter.writeChar(val);
        }
    }

    @Override
    public char[] decode(BinaryReader binaryReader) {
        char[] array = new char[binaryReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = binaryReader.readChar();
        }
        return array;
    }
}
