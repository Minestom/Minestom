package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class ShortData extends DataType<Short> {

    @Override
    public void encode(BinaryWriter writer, Short value) {
        writer.writeShort(value);
    }

    @Override
    public Short decode(BinaryReader reader) {
        return reader.readShort();
    }
}
