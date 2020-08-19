package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class DoubleArrayData extends DataType<double[]> {

    @Override
    public void encode(BinaryWriter writer, double[] value) {
        writer.writeVarInt(value.length);
        for (double val : value) {
            writer.writeDouble(val);
        }
    }

    @Override
    public double[] decode(BinaryReader reader) {
        double[] array = new double[reader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readDouble();
        }
        return array;
    }
}
