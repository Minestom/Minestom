package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DoubleData extends DataType<Double> {

    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull Double value) {
        writer.writeDouble(value);
    }

    @NotNull
    @Override
    public Double decode(@NotNull BinaryReader reader) {
        return reader.readDouble();
    }
}
