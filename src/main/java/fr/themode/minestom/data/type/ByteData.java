package fr.themode.minestom.data.type;

import fr.themode.minestom.data.DataType;

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
