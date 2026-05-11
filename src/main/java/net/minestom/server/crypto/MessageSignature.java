package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record MessageSignature(byte[] signature) {
    static final int SIGNATURE_BYTE_LENGTH = 256;

    public MessageSignature {
        if (signature.length != SIGNATURE_BYTE_LENGTH) {
            throw new IllegalArgumentException("Signature must be 256 bytes long");
        }
        signature = signature.clone();
    }

    public static final NetworkBuffer.Type<MessageSignature> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.RAW_BYTES, MessageSignature::signature,
            MessageSignature::new
    );

    public record Packed(int id, @UnknownNullability MessageSignature fullSignature) {
        private Packed(Packed packed) {
            this(packed.id, packed.fullSignature);
        }

        public static final NetworkBuffer.Type<Packed> SERIALIZER = VAR_INT.unionType(
                rawId -> {
                    int id = rawId - 1;
                    if (id == -1) return NetworkBufferTemplate.template(
                            MessageSignature.SERIALIZER, Packed::fullSignature,
                            sig -> new Packed(-1, sig));
                    return NetworkBufferTemplate.template(new Packed(id, null));
                },
                p -> p.id + 1
        );
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessageSignature(byte[] signature1))) return false;
        return Arrays.equals(signature(), signature1);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(signature());
    }
}
