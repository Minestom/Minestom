package net.minestom.server.crypto;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.utils.crypto.KeyUtils;

import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;

/**
 * Player's public key used to sign chat messages
 */
public record PlayerPublicKey(Instant expiresAt, PublicKey publicKey, byte[] signature) implements Writeable {
    public PlayerPublicKey(BinaryReader reader) {
        this(Instant.ofEpochMilli(reader.readLong()),
                KeyUtils.publicRSAKeyFrom(reader.readByteArray()), reader.readByteArray());
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeLong(expiresAt().toEpochMilli());
        writer.writeByteArray(publicKey.getEncoded());
        writer.writeByteArray(signature());
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
