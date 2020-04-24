package net.minestom.server.data.type;

import net.minestom.server.data.DataType;

import java.nio.ByteBuffer;

public class DoubleData extends DataType<Double> {

    @Override
    public byte[] encode(Double value) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.putDouble(value);
        return buffer.array();
    }

    @Override
    public Double decode(byte[] value) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.put(value);
        buffer.flip();
        return buffer.getDouble();
    }
}
