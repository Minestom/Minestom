package net.minestom.server.crypto;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.crypto.KeyUtils;
import org.jetbrains.annotations.Nullable;

import java.security.*;
import java.util.function.Consumer;

/**
 * General purpose functional interface to verify signatures.<br>
 * Built in validators:
 * <ul>
 *     <li>{@link SignatureValidator#PASS}: will always report true</li>
 *     <li>{@link SignatureValidator#FAIL}: will always report false</li>
 *     <li>{@link SignatureValidator#YGGDRASIL}: Uses SHA1 with RSA and Yggdrasil Public Key for
 *     verifying signatures</li>
 *     <li>{@link SignatureValidator#from(Player)}: Uses SHA256 with RSA and the
 *     Player's {@link PlayerPublicKey#publicKey()}</li>
 *     <li>{@link SignatureValidator#from(PublicKey, KeyUtils.SignatureAlgorithm)}: General purpose factory method</li>
 * </ul>
 */
@FunctionalInterface
public interface SignatureValidator {
    SignatureValidator PASS = (payload, signature) -> true;
    SignatureValidator FAIL = (payload, signature) -> false;
    SignatureValidator YGGDRASIL = createYggdrasilValidator();

    /**
     * Validate signature. This should not throw any exception instead it should
     * return false.
     *
     * @return true only if the signature is valid
     */
    boolean validate(byte[] payload, byte[] signature);

    default boolean validate(Consumer<BinaryWriter> payload, byte[] signature) {
        return validate(BinaryWriter.makeArray(payload), signature);
    }

    static SignatureValidator from(PublicKey publicKey, KeyUtils.SignatureAlgorithm algorithm) {
        return ((payload, signature) -> {
            try {
                final Signature sig = Signature.getInstance(algorithm.name());
                sig.initVerify(publicKey);
                sig.update(payload);
                return sig.verify(signature);
            } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                return false;
            }
        });
    }

    /**
     * Creates a validator from the player's public key using SHA256 with RSA
     *
     * @param player source of the key
     * @return null if the player didn't send a public key
     */
    static @Nullable SignatureValidator from(Player player) {
        if (player.getPlayerConnection().playerPublicKey() == null) return null;
        return from(player.getPlayerConnection().playerPublicKey().publicKey(), KeyUtils.SignatureAlgorithm.SHA256withRSA);
    }

    private static SignatureValidator createYggdrasilValidator() {
        try (var stream = SignatureValidator.class.getResourceAsStream("/yggdrasil_session_pubkey.der")) {
            if (stream == null) {
                MinecraftServer.LOGGER.error("Couldn't find Yggdrasil public key, falling back to prohibiting validator!");
                return FAIL;
            }
            return from(KeyUtils.publicRSAKeyFrom(stream.readAllBytes()), KeyUtils.SignatureAlgorithm.SHA1withRSA);
        } catch (Exception e) {
            MinecraftServer.LOGGER.error("Exception while reading Yggdrasil public key, falling back to prohibiting validator!", e);
            return FAIL;
        }
    }
}
