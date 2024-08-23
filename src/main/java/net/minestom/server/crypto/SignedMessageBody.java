package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public final class SignedMessageBody {

    public record Packed(@NotNull String content, @NotNull Instant timeStamp, long salt,
                         LastSeenMessages.@NotNull Packed lastSeen) {
        public Packed {
            if (content.length() > MessageSignature.SIGNATURE_BYTE_LENGTH) {
                throw new IllegalArgumentException("Message content too long");
            }
        }

        public static final NetworkBuffer.Type<Packed> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING, Packed::content,
                NetworkBuffer.INSTANT_MS, Packed::timeStamp,
                NetworkBuffer.LONG, Packed::salt,
                LastSeenMessages.Packed.SERIALIZER, Packed::lastSeen,
                Packed::new
        );
    }
}
