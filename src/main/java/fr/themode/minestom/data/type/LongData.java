package fr.themode.minestom.data.type;

import fr.themode.minestom.data.DataType;

import java.nio.ByteBuffer;

public class LongData extends DataType<Long> {
    @Override
    public byte[] encode(Long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        return buffer.array();
    }

    @Override
    public Long decode(byte[] value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(value);
        buffer.flip();
        return buffer.getLong();
    }
}
