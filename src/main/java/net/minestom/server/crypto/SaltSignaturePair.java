package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.Arrays;

public record SaltSignaturePair(long salt, byte[] signature) {
    public static final NetworkBuffer.Type<SaltSignaturePair> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.LONG, SaltSignaturePair::salt,
            NetworkBuffer.BYTE_ARRAY, SaltSignaturePair::signature,
            SaltSignaturePair::new
    );

    public SaltSignaturePair {
        signature = signature.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SaltSignaturePair(long salt1, byte[] signature1))) return false;
        return salt() == salt1 && Arrays.equals(signature(), signature1);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(salt());
        result = 31 * result + Arrays.hashCode(signature());
        return result;
    }
}
