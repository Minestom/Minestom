package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.FixedBitSet;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record LastSeenMessages(@NotNull List<@NotNull MessageSignature> entries) {
    public static final int MAX_ENTRIES = 20;

    public LastSeenMessages {
        entries = List.copyOf(entries);
    }

    public static final NetworkBuffer.Type<LastSeenMessages> SERIALIZER = NetworkBufferTemplate.template(
            MessageSignature.SERIALIZER.list(MAX_ENTRIES), LastSeenMessages::entries,
            LastSeenMessages::new
    );

    public record Packed(@NotNull List<MessageSignature.@NotNull Packed> entries) {
        public static final Packed EMPTY = new Packed(List.of());

        public static final NetworkBuffer.Type<Packed> SERIALIZER = NetworkBufferTemplate.template(
                MessageSignature.Packed.SERIALIZER.list(MAX_ENTRIES), Packed::entries,
                Packed::new
        );
    }

    public record Update(int offset, @NotNull BitSet acknowledged) {
        public static final NetworkBuffer.Type<Update> SERIALIZER = NetworkBufferTemplate.template(
                VAR_INT, Update::offset,
                FixedBitSet(20), Update::acknowledged,
                Update::new
        );
    }
}
