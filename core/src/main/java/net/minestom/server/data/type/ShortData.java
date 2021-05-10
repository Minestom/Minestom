package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ShortData extends DataType<Short> {

    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull Short value) {
        writer.writeShort(value);
    }

    @NotNull
    @Override
    public Short decode(@NotNull BinaryReader reader) {
        return reader.readShort();
    }
}
