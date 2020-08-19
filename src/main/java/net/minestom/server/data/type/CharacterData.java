package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class CharacterData extends DataType<Character> {

    @Override
    public void encode(BinaryWriter writer, Character value) {
        writer.writeChar(value);
    }

    @Override
    public Character decode(BinaryReader reader) {
        return reader.readChar();
    }
}
