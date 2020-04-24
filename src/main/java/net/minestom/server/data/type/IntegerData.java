package net.minestom.server.data.type;

import net.minestom.server.data.DataType;

import java.nio.ByteBuffer;

public class IntegerData extends DataType<Integer> {

    @Override
    public byte[] encode(Integer value) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);
        return buffer.array();
    }

    @Override
    public Integer decode(byte[] value) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(value);
        buffer.flip();
        return buffer.getInt();
    }
}