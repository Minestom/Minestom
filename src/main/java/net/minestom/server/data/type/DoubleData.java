package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class DoubleData extends DataType<Double> {

    @Override
    public void encode(BinaryWriter binaryWriter, Double value) {
        binaryWriter.writeDouble(value);
    }

    @Override
    public Double decode(BinaryReader binaryReader) {
        return binaryReader.readDouble();
    }
}
