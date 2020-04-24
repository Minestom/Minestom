package net.minestom.server.data.type;

import net.minestom.server.data.DataType;

import java.nio.ByteBuffer;

public class ShortData extends DataType<Short> {

    @Override
    public byte[] encode(Short value) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(value);
        return buffer.array();
    }

    @Override
    public Short decode(byte[] value) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(value);
        buffer.flip();
        return buffer.getShort();
    }
}
