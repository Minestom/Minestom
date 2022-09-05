package net.minestom.server.crypto;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public record MessageSignature(byte @NotNull [] signature) implements Writeable {
    public MessageSignature(BinaryReader reader) {
        this(reader.readByteArray());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByteArray(signature);
    }
}
