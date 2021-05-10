package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class CharacterData extends DataType<Character> {

    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull Character value) {
        writer.writeChar(value);
    }

    @NotNull
    @Override
    public Character decode(@NotNull BinaryReader reader) {
        return reader.readChar();
    }
}
