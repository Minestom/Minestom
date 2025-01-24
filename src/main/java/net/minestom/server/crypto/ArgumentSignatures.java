package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ArgumentSignatures(@NotNull List<@NotNull Entry> entries) {
    public static final int MAX_ENTRIES = 8;

    public ArgumentSignatures {
        entries = List.copyOf(entries);
    }

    public static final NetworkBuffer.Type<ArgumentSignatures> SERIALIZER = NetworkBufferTemplate.template(
            Entry.SERIALIZER.list(MAX_ENTRIES), ArgumentSignatures::entries,
            ArgumentSignatures::new
    );

    public record Entry(@NotNull String name, @NotNull MessageSignature signature) {
        public static final NetworkBuffer.Type<Entry> SERIALIZER = NetworkBufferTemplate.template(
                STRING, Entry::name,
                MessageSignature.SERIALIZER, Entry::signature,
                Entry::new
        );
    }
}
