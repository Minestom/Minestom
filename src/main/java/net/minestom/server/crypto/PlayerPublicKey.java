package net.minestom.server.crypto;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.utils.crypto.KeyUtils;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;

/**
 * Player's public key used to sign chat messages
 *
 * @param expiresAt signature expiration date
 * @param publicKey the player's public key
 * @param signature {@link #signedPayload()} signed by Yggdrasil
 */
public record PlayerPublicKey(Instant expiresAt, PublicKey publicKey, byte[] signature) implements Writeable {

    public PlayerPublicKey(BinaryReader reader) {
        this(Instant.ofEpochMilli(reader.readLong()),
                KeyUtils.publicRSAKeyFrom(reader.readByteArray()), reader.readByteArray());
    }

    public byte[] signedPayload() {
        return (expiresAt.toEpochMilli() + KeyUtils.rsaPublicKeyToString(publicKey)).getBytes(StandardCharsets.US_ASCII);
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isValid() {
        return !isExpired() && SignatureValidator.YGGDRASIL_VALIDATOR.validate(signedPayload(), signature());
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeLong(expiresAt().toEpochMilli());
        writer.writeByteArray(publicKey.getEncoded());
        writer.writeByteArray(signature());
    }
}
