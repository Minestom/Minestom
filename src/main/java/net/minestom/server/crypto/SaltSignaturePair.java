package net.minestom.server.crypto;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public record SaltSignaturePair(long salt, byte[] signature) implements Writeable {
    public SaltSignaturePair(BinaryReader reader) {
        this(reader.readLong(), reader.readByteArray());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(salt);
        writer.writeByteArray(signature);
    }
}
