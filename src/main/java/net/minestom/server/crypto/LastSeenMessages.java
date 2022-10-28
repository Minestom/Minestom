package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
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

    public LastSeenMessages(NetworkBuffer reader) {
        this(reader.readCollection(Entry::new));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {

    }

    public record Entry(UUID from, MessageSignature lastSignature) implements Writeable {
        public Entry(NetworkBuffer reader) {
            this(reader.read(NetworkBuffer.UUID), new MessageSignature(reader));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeUuid(from);
            writer.write(lastSignature);
        }
    }

    public record Update(LastSeenMessages lastSeen, @Nullable Entry lastReceived) implements Writeable {
        public Update(NetworkBuffer reader) {
            this(new LastSeenMessages(reader), reader.readOptional(Entry::new));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.write(lastSeen);
            writer.writeBoolean(lastReceived != null);
            if (lastReceived != null) writer.write(lastReceived);
        }
    }
}
