package net.minestom.server.crypto;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ArgumentSignatures(@NotNull List<@NotNull Entry> entries) implements Writeable {
    public ArgumentSignatures {
        entries = List.copyOf(entries);
    }

    public ArgumentSignatures(BinaryReader reader) {
        this(reader.readVarIntList(Entry::new));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarIntList(entries, BinaryWriter::write);
    }

    public record Entry(@NotNull String name, @NotNull MessageSignature signature) implements Writeable {
        public Entry(BinaryReader reader) {
            this(reader.readSizedString(), new MessageSignature(reader));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(name);
            writer.write(signature);
        }
    }
}
