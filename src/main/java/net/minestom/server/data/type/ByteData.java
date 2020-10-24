package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ByteData extends DataType<Byte> {
    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull Byte value) {
        writer.writeByte(value);
    }

    @NotNull
    @Override
    public Byte decode(@NotNull BinaryReader reader) {
        return reader.readByte();
    }
}
