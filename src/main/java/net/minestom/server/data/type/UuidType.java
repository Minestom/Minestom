package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UuidType extends DataType<UUID> {
    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull UUID value) {
        writer.writeUuid(value);
    }

    @NotNull
    @Override
    public UUID decode(@NotNull BinaryReader reader) {
        return reader.readUuid();
    }
}
