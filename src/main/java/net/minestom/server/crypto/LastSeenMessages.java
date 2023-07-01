package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record LastSeenMessages(@NotNull List<@NotNull MessageSignature> entries) implements NetworkBuffer.Writer {
    public LastSeenMessages {
        entries = List.copyOf(entries);
    }

    public LastSeenMessages(@NotNull NetworkBuffer reader) {
        this(reader.readCollection(MessageSignature::new));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

    public record Packed(@NotNull List<MessageSignature.@NotNull Packed> entries) implements NetworkBuffer.Writer {
        public static final Packed EMPTY = new Packed(List.of());

        public Packed(@NotNull NetworkBuffer reader) {
            this(reader.readCollection(MessageSignature.Packed::new));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.writeCollection(entries);
        }
    }

    public record Update(int offset, @NotNull BitSet acknowledged) implements NetworkBuffer.Writer {
        public Update(@NotNull NetworkBuffer reader) {
            this(reader.read(VAR_INT), reader.readFixedBitSet(20));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(VAR_INT, offset);
            writer.writeFixedBitSet(acknowledged, 20);
        }
    }
}
