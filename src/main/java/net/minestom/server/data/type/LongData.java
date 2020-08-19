package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class LongData extends DataType<Long> {
    @Override
    public void encode(BinaryWriter binaryWriter, Long value) {
        binaryWriter.writeLong(value);
    }

    @Override
    public Long decode(BinaryReader binaryReader) {
        return binaryReader.readLong();
    }
}
