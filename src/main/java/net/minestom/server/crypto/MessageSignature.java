package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.FixedRawBytes;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record MessageSignature(byte[] signature) {
    static final int SIGNATURE_BYTE_LENGTH = 256;

    public MessageSignature {
        if (signature.length != SIGNATURE_BYTE_LENGTH) {
            throw new IllegalArgumentException("Signature must be 256 bytes long");
        }
    }

    public static final NetworkBuffer.Type<MessageSignature> SERIALIZER = NetworkBufferTemplate.template(
            FixedRawBytes(SIGNATURE_BYTE_LENGTH), MessageSignature::signature,
            MessageSignature::new
    );

    public record Packed(int id, @UnknownNullability MessageSignature fullSignature) {
        private static final int FULL_SIGNATURE = -1;

        public Packed(MessageSignature signature) {
            this(FULL_SIGNATURE, signature);
        }

        public Packed {
            Check.argCondition(id == FULL_SIGNATURE && fullSignature == null, "Full signature must be present");
        }

        public static final NetworkBuffer.Type<Packed> SERIALIZER = new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, Packed value) {
                buffer.write(VAR_INT, value.id + 1);
                if (value.fullSignature != null) buffer.write(MessageSignature.SERIALIZER, value.fullSignature);
            }

            @Override
            public Packed read(NetworkBuffer buffer) {
                final int id = buffer.read(VAR_INT) - 1;
                return id == FULL_SIGNATURE ? new MessageSignature.Packed(buffer.read(MessageSignature.SERIALIZER))
                        : new MessageSignature.Packed(id, null);
            }
        };
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
