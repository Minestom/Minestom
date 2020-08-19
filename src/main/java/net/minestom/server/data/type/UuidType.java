package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.UUID;

public class UuidType extends DataType<UUID> {
    @Override
    public void encode(BinaryWriter writer, UUID value) {
        writer.writeUuid(value);
    }

    @Override
    public UUID decode(BinaryReader reader) {
        return reader.readUuid();
    }
}
