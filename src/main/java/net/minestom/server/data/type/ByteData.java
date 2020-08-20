package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class ByteData extends DataType<Byte> {
    @Override
    public void encode(BinaryWriter writer, Byte value) {
        writer.writeByte(value);
    }

    @Override
    public Byte decode(BinaryReader reader) {
        return reader.readByte();
    }
}
