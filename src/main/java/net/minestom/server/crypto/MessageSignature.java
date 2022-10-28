package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;

public record MessageSignature(byte @NotNull [] signature) implements Writeable {
    public MessageSignature(NetworkBuffer reader) {
        this(reader.read(BYTE_ARRAY));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByteArray(signature);
    }
}
