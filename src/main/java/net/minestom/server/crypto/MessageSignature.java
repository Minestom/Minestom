package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record MessageSignature(byte @NotNull [] signature) {
    static final int SIGNATURE_BYTE_LENGTH = 256;

    public MessageSignature {
        if (signature.length != SIGNATURE_BYTE_LENGTH) {
            throw new IllegalArgumentException("Signature must be 256 bytes long");
        }
    }

    public static final NetworkBuffer.Type<MessageSignature> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.RAW_BYTES, MessageSignature::signature,
            MessageSignature::new
    );

    public record Packed(int id, @UnknownNullability MessageSignature fullSignature) {
        private Packed(@NotNull Packed packed) {
            this(packed.id, packed.fullSignature);
        }

        public static final NetworkBuffer.Type<Packed> SERIALIZER = new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Packed value) {
                buffer.write(VAR_INT, value.id + 1);
                if (value.id == 0) buffer.write(MessageSignature.SERIALIZER, value.fullSignature);
            }

            @Override
            public Packed read(@NotNull NetworkBuffer buffer) {
                final int id = buffer.read(VAR_INT) - 1;
                return new Packed(id, id == -1 ? buffer.read(MessageSignature.SERIALIZER) : null);
            }
        };
    }
}
