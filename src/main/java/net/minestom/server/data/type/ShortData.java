package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class ShortData extends DataType<Short> {

    @Override
    public void encode(BinaryWriter binaryWriter, Short value) {
        binaryWriter.writeShort(value);
    }

    @Override
    public Short decode(BinaryReader binaryReader) {
        return binaryReader.readShort();
    }
}
