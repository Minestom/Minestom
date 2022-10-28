package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record SignedMessageHeader(@Nullable MessageSignature previousSignature,
                                  @NotNull UUID sender) implements NetworkBuffer.Writer {
    public SignedMessageHeader(@NotNull NetworkBuffer reader) {
        this(reader.readOptional(MessageSignature::new), reader.read(NetworkBuffer.UUID));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeOptional(previousSignature);
        writer.write(NetworkBuffer.UUID, sender);
    }
}
