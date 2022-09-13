package net.minestom.server.crypto;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record LastSeenMessages(@NotNull List<@NotNull Entry> entries) implements Writeable {
    public LastSeenMessages {
        entries = List.copyOf(entries);
    }

    public LastSeenMessages(BinaryReader reader) {
        this(reader.readVarIntList(Entry::new));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {

    }

    public record Entry(UUID from, MessageSignature lastSignature) implements Writeable {
        public Entry(BinaryReader reader) {
            this(reader.readUuid(), new MessageSignature(reader));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeUuid(from);
            writer.write(lastSignature);
        }
    }

    public record Update(LastSeenMessages lastSeen, @Nullable Entry lastReceived) implements Writeable {
        public Update(BinaryReader reader) {
            this(new LastSeenMessages(reader), reader.readBoolean() ? new Entry(reader) : null);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.write(lastSeen);
            writer.writeBoolean(lastReceived != null);
            if (lastReceived != null) writer.write(lastReceived);
        }
    }
}
