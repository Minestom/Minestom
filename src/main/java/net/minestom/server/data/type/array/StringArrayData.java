package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class StringArrayData extends DataType<String[]> {

    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull String[] value) {
        writer.writeStringArray(value);
    }

    @NotNull
    @Override
    public String[] decode(@NotNull BinaryBuffer reader) {
        return reader.readSizedStringArray(Integer.MAX_VALUE);
    }
}
