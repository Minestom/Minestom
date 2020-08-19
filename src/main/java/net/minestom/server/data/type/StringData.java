package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class StringData extends DataType<String> {

    @Override
    public void encode(BinaryWriter binaryWriter, String value) {
        binaryWriter.writeSizedString(value);
    }

    @Override
    public String decode(BinaryReader binaryReader) {
        return binaryReader.readSizedString();
    }
}
