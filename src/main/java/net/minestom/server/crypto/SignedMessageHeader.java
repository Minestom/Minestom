package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record SignedMessageHeader(@Nullable MessageSignature previousSignature, UUID sender) {
    public static final NetworkBuffer.Type<SignedMessageHeader> SERIALIZER = NetworkBufferTemplate.template(
            MessageSignature.SERIALIZER.optional(), SignedMessageHeader::previousSignature,
            NetworkBuffer.UUID, SignedMessageHeader::sender,
            SignedMessageHeader::new
    );
}
