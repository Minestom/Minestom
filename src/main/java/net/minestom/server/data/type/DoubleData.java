package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class DoubleData extends DataType<Double> {

    @Override
    public void encode(BinaryWriter writer, Double value) {
        writer.writeDouble(value);
    }

    @Override
    public Double decode(BinaryReader reader) {
        return reader.readDouble();
    }
}
