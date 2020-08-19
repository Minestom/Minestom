package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class LongData extends DataType<Long> {
    @Override
    public void encode(BinaryWriter writer, Long value) {
        writer.writeLong(value);
    }

    @Override
    public Long decode(BinaryReader reader) {
        return reader.readLong();
    }
}
