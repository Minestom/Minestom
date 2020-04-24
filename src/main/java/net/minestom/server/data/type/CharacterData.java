package net.minestom.server.data.type;

import net.minestom.server.data.DataType;

import java.nio.ByteBuffer;

public class CharacterData extends DataType<Character> {

    @Override
    public byte[] encode(Character value) {
        ByteBuffer buffer = ByteBuffer.allocate(Character.BYTES);
        buffer.putChar(value);
        return buffer.array();
    }

    @Override
    public Character decode(byte[] value) {
        ByteBuffer buffer = ByteBuffer.allocate(Character.BYTES);
        buffer.put(value);
        buffer.flip();
        return buffer.getChar();
    }
}
