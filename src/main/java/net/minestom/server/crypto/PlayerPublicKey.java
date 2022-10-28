package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.crypto.KeyUtils;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;
import static net.minestom.server.network.NetworkBuffer.LONG;

/**
 * Player's public key used to sign chat messages
 */
public record PlayerPublicKey(Instant expiresAt, PublicKey publicKey,
                              byte[] signature) implements NetworkBuffer.Writer {
    public PlayerPublicKey(@NotNull NetworkBuffer reader) {
        this(Instant.ofEpochMilli(reader.read(LONG)),
                KeyUtils.publicRSAKeyFrom(reader.read(BYTE_ARRAY)), reader.read(BYTE_ARRAY));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG, expiresAt().toEpochMilli());
        writer.write(BYTE_ARRAY, publicKey.getEncoded());
        writer.write(BYTE_ARRAY, signature());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlayerPublicKey ppk) {
            return expiresAt.equals(ppk.expiresAt) && publicKey.equals(ppk.publicKey) && Arrays.equals(signature, ppk.signature);
        } else {
            return false;
        }
    }
}
