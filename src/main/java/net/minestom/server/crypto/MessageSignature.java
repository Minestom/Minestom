package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record MessageSignature(byte @NotNull [] signature) implements NetworkBuffer.Writer {

    static final int SIGNATURE_BYTE_LENGTH = 256;

    public MessageSignature {
        if (signature.length != SIGNATURE_BYTE_LENGTH) {
            throw new IllegalArgumentException("Signature must be 256 bytes long");
        }
    }

    public MessageSignature(@NotNull NetworkBuffer reader) {
        this(reader.readBytes(SIGNATURE_BYTE_LENGTH));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(RAW_BYTES, signature);
    }

    public record Packed(int id, @UnknownNullability MessageSignature fullSignature) implements NetworkBuffer.Writer {
        public Packed(@NotNull NetworkBuffer reader) {
            this(read(reader));
        }

        private Packed(@NotNull Packed packed) {
            this(packed.id, packed.fullSignature);
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(VAR_INT, id + 1);
            if (id == 0) writer.write(fullSignature);
        }

        private static Packed read(NetworkBuffer reader) {
            final int id = reader.read(VAR_INT) - 1;
            return new Packed(id, id == -1 ? new MessageSignature(reader) : null);
        }
    }
}
