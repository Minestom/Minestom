package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;
import static net.minestom.server.network.NetworkBuffer.LONG;

public record SaltSignaturePair(long salt, byte[] signature) implements NetworkBuffer.Writer {
    public SaltSignaturePair(@NotNull NetworkBuffer reader) {
        this(reader.read(LONG), reader.read(BYTE_ARRAY));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG, salt);
        writer.write(BYTE_ARRAY, signature);
    }
}
