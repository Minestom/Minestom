package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class FloatData extends DataType<Float> {

    @Override
    public void encode(BinaryWriter binaryWriter, Float value) {
        binaryWriter.writeFloat(value);
    }

    @Override
    public Float decode(BinaryReader binaryReader) {
        return binaryReader.readFloat();
    }
}
