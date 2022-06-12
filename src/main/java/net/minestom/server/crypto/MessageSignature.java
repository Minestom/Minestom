package net.minestom.server.crypto;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

public record MessageSignature(UUID signer, Instant timestamp, long salt, byte[] signature) implements Writeable {
    public static final UUID UNSIGNED_SENDER = new UUID(0,0);
    public static final MessageSignature UNSIGNED = new MessageSignature(UNSIGNED_SENDER, Instant.ofEpochMilli(0), 0, new byte[0]);

    public MessageSignature(UUID signer, BinaryReader reader) {
        this(signer, Instant.ofEpochMilli(reader.readLong()), reader.readLong(), reader.readByteArray());
    }

    @Contract("_ -> new")
    public MessageSignature withSigner(UUID uuid) {
        return new MessageSignature(uuid, timestamp, salt, signature);
    }

    public boolean unsigned() {
        return signer == UNSIGNED_SENDER;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(timestamp.toEpochMilli());
        writer.writeLong(salt);
        writer.writeByteArray(signature);
    }
}
