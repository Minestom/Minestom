package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class BooleanData extends DataType<Boolean> {
    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull Boolean value) {
        writer.writeBoolean(value);
    }

    @NotNull
    @Override
    public Boolean decode(@NotNull BinaryReader reader) {
        return reader.readBoolean();
    }
}
