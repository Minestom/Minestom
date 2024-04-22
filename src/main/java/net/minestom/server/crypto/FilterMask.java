package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

import static net.minestom.server.network.NetworkBuffer.LONG_ARRAY;

public record FilterMask(@NotNull Type type, @NotNull BitSet mask) implements NetworkBuffer.Writer {
    public FilterMask(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(Type.class), BitSet.valueOf(reader.read(LONG_ARRAY)));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(Type.class, type);
        if (type == Type.PARTIALLY_FILTERED) {
            writer.write(LONG_ARRAY, mask.toLongArray());
        }
    }

    public enum Type {
        PASS_THROUGH,
        FULLY_FILTERED,
        PARTIALLY_FILTERED
    }
}
