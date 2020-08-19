package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class DoubleArrayData extends DataType<double[]> {

    @Override
    public void encode(BinaryWriter binaryWriter, double[] value) {
        binaryWriter.writeVarInt(value.length);
        for (double val : value) {
            binaryWriter.writeDouble(val);
        }
    }

    @Override
    public double[] decode(BinaryReader binaryReader) {
        double[] array = new double[binaryReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = binaryReader.readDouble();
        }
        return array;
    }
}
