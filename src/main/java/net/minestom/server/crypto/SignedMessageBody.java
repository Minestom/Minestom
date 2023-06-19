package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public final class SignedMessageBody {

    public record Packed(@NotNull String content, @NotNull Instant timeStamp, long salt,
                         LastSeenMessages.@NotNull Packed lastSeen) implements NetworkBuffer.Writer {
        public Packed {
            if (content.length() > MessageSignature.SIGNATURE_BYTE_LENGTH) {
                throw new IllegalArgumentException("Message content too long");
            }
        }

        public Packed(@NotNull NetworkBuffer reader) {
            this(reader.read(NetworkBuffer.STRING), Instant.ofEpochMilli(reader.read(NetworkBuffer.LONG)),
                    reader.read(NetworkBuffer.LONG), new LastSeenMessages.Packed(reader));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.STRING, content);
            writer.write(NetworkBuffer.LONG, timeStamp.toEpochMilli());
            writer.write(NetworkBuffer.LONG, salt);
            writer.write(lastSeen);
        }
    }
}
