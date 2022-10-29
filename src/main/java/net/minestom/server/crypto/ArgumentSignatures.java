package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ArgumentSignatures(@NotNull List<@NotNull Entry> entries) implements NetworkBuffer.Writer {
    public ArgumentSignatures {
        entries = List.copyOf(entries);
    }

    public ArgumentSignatures(@NotNull NetworkBuffer reader) {
        this(reader.readCollection(Entry::new));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(entries);
    }

    public record Entry(@NotNull String name, @NotNull MessageSignature signature) implements NetworkBuffer.Writer {
        public Entry(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), new MessageSignature(reader));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, name);
            writer.write(signature);
        }
    }
}
