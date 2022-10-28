package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ArgumentSignatures(@NotNull List<@NotNull Entry> entries) implements Writeable {
    public ArgumentSignatures {
        entries = List.copyOf(entries);
    }

    public ArgumentSignatures(NetworkBuffer reader) {
        this(reader.readCollection(Entry::new));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarIntList(entries, BinaryWriter::write);
    }

    public record Entry(@NotNull String name, @NotNull MessageSignature signature) implements Writeable {
        public Entry(NetworkBuffer reader) {
            this(reader.read(STRING), new MessageSignature(reader));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(name);
            writer.write(signature);
        }
    }
}
