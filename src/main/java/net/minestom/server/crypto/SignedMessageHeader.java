package net.minestom.server.crypto;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record SignedMessageHeader(@Nullable MessageSignature previousSignature, UUID sender) implements Writeable {
    public SignedMessageHeader(BinaryReader reader) {
        this(reader.readBoolean() ? new MessageSignature(reader) : null, reader.readUuid());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBoolean(previousSignature != null);
        if (previousSignature != null) writer.write(previousSignature);
        writer.writeUuid(sender);
    }
}
