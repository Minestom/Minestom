package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class FloatArrayData extends DataType<float[]> {

    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull float[] value) {
        writer.writeVarInt(value.length);
        for (float val : value) {
            writer.writeFloat(val);
        }
    }

    @NotNull
    @Override
    public float[] decode(@NotNull BinaryReader reader) {
        float[] array = new float[reader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readFloat();
        }
        return array;
    }
}
