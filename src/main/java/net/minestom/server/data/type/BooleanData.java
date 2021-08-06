package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class BooleanData extends DataType<Boolean> {
    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull Boolean value) {
        writer.writeBoolean(value);
    }

    @NotNull
    @Override
    public Boolean decode(@NotNull BinaryBuffer reader) {
        return reader.readBoolean();
    }
}
