package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class FloatData extends DataType<Float> {

    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull Float value) {
        writer.writeFloat(value);
    }

    @NotNull
    @Override
    public Float decode(@NotNull BinaryBuffer reader) {
        return reader.readFloat();
    }
}
