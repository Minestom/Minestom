package net.minestom.server.crypto;

import net.minestom.server.utils.crypto.KeyUtils;

import java.io.IOException;
import java.security.*;

@FunctionalInterface
public interface SignatureValidator {
    SignatureValidator NO_VALIDATOR = (payload, signature) -> true;
    SignatureValidator YGGDRASIL_VALIDATOR = createYggdrasilValidator();

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

    private static SignatureValidator createYggdrasilValidator() {
        try {
            return from(KeyUtils.publicRSAKeyFrom(SignatureValidator.class.getResourceAsStream("/yggdrasil_session_pubkey.der").readAllBytes()),
                    KeyUtils.SignatureAlgorithm.SHA1withRSA);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
