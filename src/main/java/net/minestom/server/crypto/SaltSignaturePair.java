package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;
import static net.minestom.server.network.NetworkBuffer.LONG;

public record SaltSignaturePair(long salt, byte[] signature) implements Writeable {
    public SaltSignaturePair(NetworkBuffer reader) {
        this(reader.read(LONG), reader.read(BYTE_ARRAY));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(salt);
        writer.writeByteArray(signature);
    }
}
