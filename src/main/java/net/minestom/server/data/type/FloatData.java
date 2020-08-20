package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class FloatData extends DataType<Float> {

    @Override
    public void encode(BinaryWriter writer, Float value) {
        writer.writeFloat(value);
    }

    @Override
    public Float decode(BinaryReader reader) {
        return reader.readFloat();
    }
}
