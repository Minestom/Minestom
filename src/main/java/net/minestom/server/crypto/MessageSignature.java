package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.UnknownNullability;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record MessageSignature(byte[] signature) {
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
        private Packed(Packed packed) {
            this(packed.id, packed.fullSignature);
        }

        public static final NetworkBuffer.Type<Packed> SERIALIZER = NetworkBuffer.Tagged(
                VAR_INT, p -> p.id + 1,
                rawId -> {
                    int id = rawId - 1;
                    if (id == -1) return NetworkBufferTemplate.template(
                            MessageSignature.SERIALIZER, Packed::fullSignature,
                            sig -> new Packed(-1, sig));
                    return NetworkBufferTemplate.template(new Packed(id, null));
                }
        );
    }
}
