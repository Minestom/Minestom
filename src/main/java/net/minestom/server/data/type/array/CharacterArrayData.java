package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class CharacterArrayData extends DataType<char[]> {
    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull char[] value) {
        writer.writeVarInt(value.length);
        for (char val : value) {
            writer.writeChar(val);
        }
    }

    @NotNull
    @Override
    public char[] decode(@NotNull BinaryBuffer reader) {
        char[] array = new char[reader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readChar();
        }
        return array;
    }
}
