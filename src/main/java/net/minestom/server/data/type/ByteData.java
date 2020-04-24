package net.minestom.server.data.type;

import net.minestom.server.data.DataType;

public class ByteData extends DataType<Byte> {
    @Override
    public byte[] encode(Byte value) {
        return new byte[]{value};
    }

    @Override
    public Byte decode(byte[] value) {
        return value[0];
    }
}
