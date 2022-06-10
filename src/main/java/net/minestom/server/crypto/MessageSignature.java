package net.minestom.server.crypto;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

public record MessageSignature(Instant timestamp, long salt, byte[] signature) implements Writeable {
    public static final UUID UNSIGNED_SENDER = new UUID(0,0);
    public static final MessageSignature UNSIGNED = new MessageSignature(Instant.ofEpochMilli(0), 0, new byte[0]);

    public MessageSignature(BinaryReader reader) {
        this(Instant.ofEpochMilli(reader.readLong()), reader.readLong(), reader.readByteArray());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(timestamp.toEpochMilli());
        writer.writeLong(salt);
        writer.writeByteArray(signature);
    }
}
