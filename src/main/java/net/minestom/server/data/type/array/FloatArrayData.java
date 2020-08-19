package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class FloatArrayData extends DataType<float[]> {

    @Override
    public void encode(BinaryWriter binaryWriter, float[] value) {
        binaryWriter.writeVarInt(value.length);
        for (float val : value) {
            binaryWriter.writeFloat(val);
        }
    }

    @Override
    public float[] decode(BinaryReader binaryReader) {
        float[] array = new float[binaryReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = binaryReader.readFloat();
        }
        return array;
    }
}
