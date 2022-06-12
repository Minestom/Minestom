package net.minestom.server.crypto;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.utils.crypto.KeyUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.*;

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

    static SignatureValidator from(PublicKey publicKey, KeyUtils.SignatureAlgorithm algorithm) {
        return ((payload, signature) -> {
            try {
                // TODO Check overhead associated with creating a new instance for every verification
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
     * Can be used to verify signatures of {@link net.minestom.server.network.packet.client.play.ClientChatMessagePacket}
     * and {@link ClientCommandChatPacket#signatures()}. For command args the signature is generated for the given
     * value wrapped in {@link Component#text(String)} or for the preview if it's present regardless of the arg node
     * name. Vanilla implementation of argument signing can be found at (Mojang mappings):
     * <i>net.minecraft.client.player.LocalPlayer#signCommandArguments</i><br>
     *
     * @param validator validator acquired from {@link SignatureValidator#from(Player)}
     * @param signature signature data
     * @param component the component that was signed
     * @return true if the signature is valid
     */
    static boolean validate(SignatureValidator validator, MessageSignature signature, Component component) {
        final byte[] componentBytes = GsonComponentSerializer.gson().serialize(component).getBytes(StandardCharsets.UTF_8);
        byte[] signerDetails = new byte[32+ componentBytes.length];
        ByteBuffer bytebuffer = ByteBuffer.wrap(signerDetails).order(ByteOrder.BIG_ENDIAN);
        bytebuffer.putLong(signature.salt());
        bytebuffer.putLong(signature.signer().getMostSignificantBits()).putLong(signature.signer().getLeastSignificantBits());
        bytebuffer.putLong(signature.timestamp().getEpochSecond());
        bytebuffer.put(componentBytes);
        return validator.validate(bytebuffer.array(), signature.signature());
    }

    /**
     * Creates a validator from the player's public key using SHA256 with RSA
     *
     * @param player source of the key
     * @return null if the player didn't send a public key
     */
    static @Nullable SignatureValidator from(Player player) {
        if (player.getPlayerConnection().getPlayerPublicKey() == null) return null;
        return from(player.getPlayerConnection().getPlayerPublicKey().publicKey(), KeyUtils.SignatureAlgorithm.SHA256withRSA);
    }

    private static SignatureValidator createYggdrasilValidator() {
        try {
            return from(KeyUtils.publicRSAKeyFrom(SignatureValidator.class.getResourceAsStream("/yggdrasil_session_pubkey.der").readAllBytes()),
                    KeyUtils.SignatureAlgorithm.SHA1withRSA);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
