package net.minestom.server.crypto;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.crypto.KeyUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.UUID;

@FunctionalInterface
public interface SignatureValidator {
    SignatureValidator PASS = (payload, signature) -> true;
    SignatureValidator FAIL = (payload, signature) -> false;
    SignatureValidator YGGDRASIL = createYggdrasilValidator();

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

    static boolean validate(SignatureValidator validator, UUID signer, MessageSignature signature, Component component) {
        final byte[] componentBytes = GsonComponentSerializer.gson().serialize(component).getBytes(StandardCharsets.UTF_8);
        byte[] signerDetails = new byte[32+ componentBytes.length];
        ByteBuffer bytebuffer = ByteBuffer.wrap(signerDetails).order(ByteOrder.BIG_ENDIAN);
        bytebuffer.putLong(signature.salt());
        bytebuffer.putLong(signer.getMostSignificantBits()).putLong(signer.getLeastSignificantBits());
        bytebuffer.putLong(signature.timestamp().getEpochSecond());
        bytebuffer.put(componentBytes);
        return validator.validate(bytebuffer.array(), signature.signature());
    }

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
