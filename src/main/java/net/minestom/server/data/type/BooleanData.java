package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class BooleanData extends DataType<Boolean> {
    @Override
    public void encode(BinaryWriter binaryWriter, Boolean value) {
        binaryWriter.writeBoolean(value);
    }

    @Override
    public Boolean decode(BinaryReader binaryReader) {
        return binaryReader.readBoolean();
    }
}
