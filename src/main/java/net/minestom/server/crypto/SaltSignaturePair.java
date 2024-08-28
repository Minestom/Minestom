package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

public record SaltSignaturePair(long salt, byte[] signature) {
    public static final NetworkBuffer.Type<SaltSignaturePair> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.LONG, SaltSignaturePair::salt,
            NetworkBuffer.BYTE_ARRAY, SaltSignaturePair::signature,
            SaltSignaturePair::new
    );
}
