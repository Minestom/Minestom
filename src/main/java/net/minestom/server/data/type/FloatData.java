package net.minestom.server.data.type;

import net.minestom.server.data.DataType;

import java.nio.ByteBuffer;

public class FloatData extends DataType<Float> {

    @Override
    public byte[] encode(Float value) {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.putFloat(value);
        return buffer.array();
    }

    @Override
    public Float decode(byte[] value) {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.put(value);
        buffer.flip();
        return buffer.getFloat();
    }
}
