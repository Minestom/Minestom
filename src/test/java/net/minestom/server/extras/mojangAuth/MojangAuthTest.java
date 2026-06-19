package net.minestom.server.extras.mojangAuth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.player.GameProfile;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Demonstrates that the online-login crypto can be exercised without booting a {@code MinecraftServer}:
 * a synthetic keypair is all that's needed to drive {@link MojangAuth#verifyEncryptionResponse},
 * {@link MojangAuth#serverIdHash} and {@link MojangAuth#parseProfile}.
 */
class MojangAuthTest {

    static {
        // Workaround for a latent class-init cycle in production code:
        // GameProfile.Property.CODEC → Codec → ComponentCodecs → ResolvableProfile.Partial.CODEC → Property.LIST_CODEC.
        // If Property is the entry point, LIST_CODEC is observed as null mid-init and the class fails to load.
        // Forcing Codec to initialize first lets Property complete safely because Codec.STRING (line 105)
        // is set well before line 121, which is where the cycle into ComponentCodecs begins.
        try {
            Class.forName(net.minestom.server.codec.Codec.class.getName());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final UUID NOTCH_UUID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
    private static final String NOTCH_DASHLESS = "069a79f444e94726a5befca90e38aaf5";

    @Test
    void parses_profile_with_signed_and_unsigned_properties() {
        final JsonObject json = JsonParser.parseString("""
                {"id":"%s","name":"Notch","properties":[
                  {"name":"textures","value":"X","signature":"Y"},
                  {"name":"plain","value":"Z"}
                ]}""".formatted(NOTCH_DASHLESS)).getAsJsonObject();

        final GameProfile profile = MojangAuth.parseProfile(json);
        assertEquals("Notch", profile.name());
        assertEquals(NOTCH_UUID, profile.uuid());

        final List<GameProfile.Property> properties = profile.properties();
        assertEquals(2, properties.size());
        assertEquals(new GameProfile.Property("textures", "X", "Y"), properties.get(0));
        assertEquals("plain", properties.get(1).name());
        assertEquals("Z", properties.get(1).value());
    }

    @Test
    void login_challenge_create_uses_supplied_rng() {
        // Deterministic RNG → deterministic nonce. Proves create() doesn't reach into ThreadLocalRandom.
        final RandomGenerator rng = new java.util.Random(0xC0FFEEL);
        final byte[] expected = new byte[4];
        new java.util.Random(0xC0FFEEL).nextBytes(expected);

        final MojangAuth.LoginChallenge challenge = MojangAuth.LoginChallenge.create("Steve", rng);
        assertEquals("Steve", challenge.username());
        assertArrayEquals(expected, challenge.nonce());
    }

    @Test
    void server_id_hash_is_stable_for_same_inputs() throws Exception {
        final KeyPair kp = MojangCrypt.generateKeyPair();
        final SecretKey aes = KeyGenerator.getInstance("AES").generateKey();

        final String first = MojangAuth.serverIdHash(kp.getPublic(), aes);
        final String second = MojangAuth.serverIdHash(kp.getPublic(), aes);
        assertEquals(first, second);
        assertNotNull(first);
    }

    @Test
    void verifies_encryption_response_and_recovers_shared_secret() throws Exception {
        final KeyPair kp = MojangCrypt.generateKeyPair();
        final SecretKey aes = KeyGenerator.getInstance("AES").generateKey();
        final byte[] nonce = {7, 7, 7, 7};

        final ClientEncryptionResponsePacket packet = new ClientEncryptionResponsePacket(
                rsaEncrypt(kp.getPublic(), aes.getEncoded()),
                rsaEncrypt(kp.getPublic(), nonce));
        final MojangAuth.LoginChallenge challenge = new MojangAuth.LoginChallenge("Steve", nonce);

        final SecretKey recovered = MojangAuth.verifyEncryptionResponse(kp, challenge, packet, false);
        assertArrayEquals(aes.getEncoded(), recovered.getEncoded());
        // serverId is derived purely from the recovered secret + server key, so it matches what the client computed.
        assertEquals(MojangAuth.serverIdHash(kp.getPublic(), aes), MojangAuth.serverIdHash(kp.getPublic(), recovered));
    }

    @Test
    void rejects_when_verify_token_does_not_match_nonce() throws Exception {
        final KeyPair kp = MojangCrypt.generateKeyPair();
        final SecretKey aes = KeyGenerator.getInstance("AES").generateKey();

        final ClientEncryptionResponsePacket packet = new ClientEncryptionResponsePacket(
                rsaEncrypt(kp.getPublic(), aes.getEncoded()),
                rsaEncrypt(kp.getPublic(), new byte[]{9, 9, 9, 9}));
        final MojangAuth.LoginChallenge challenge = new MojangAuth.LoginChallenge("Steve", new byte[]{1, 2, 3, 4});

        assertThrows(MojangAuth.AuthException.class, () ->
                MojangAuth.verifyEncryptionResponse(kp, challenge, packet, false));
    }

    @Test
    void rejects_when_client_carries_player_public_key() throws Exception {
        // Legacy chat-signing path: modern clients don't send a player public key.
        // If one is present we refuse the encryption response even if the nonce decrypts correctly.
        final KeyPair kp = MojangCrypt.generateKeyPair();
        final SecretKey aes = KeyGenerator.getInstance("AES").generateKey();
        final byte[] nonce = {1, 2, 3, 4};

        final ClientEncryptionResponsePacket packet = new ClientEncryptionResponsePacket(
                rsaEncrypt(kp.getPublic(), aes.getEncoded()),
                rsaEncrypt(kp.getPublic(), nonce));
        final MojangAuth.LoginChallenge challenge = new MojangAuth.LoginChallenge("Steve", nonce);

        assertThrows(MojangAuth.AuthException.class, () ->
                MojangAuth.verifyEncryptionResponse(kp, challenge, packet, /*clientHasPublicKey=*/ true));
    }

    @Test
    void rejects_when_verify_token_is_garbage() throws Exception {
        final KeyPair kp = MojangCrypt.generateKeyPair();
        final SecretKey aes = KeyGenerator.getInstance("AES").generateKey();

        // Garbage verify token that won't even RSA-decrypt cleanly
        final byte[] garbage = new byte[128];
        final ClientEncryptionResponsePacket packet = new ClientEncryptionResponsePacket(
                rsaEncrypt(kp.getPublic(), aes.getEncoded()), garbage);
        final MojangAuth.LoginChallenge challenge = new MojangAuth.LoginChallenge("Steve", new byte[]{1, 2, 3, 4});

        assertThrows(MojangAuth.AuthException.class, () ->
                MojangAuth.verifyEncryptionResponse(kp, challenge, packet, false));
    }

    private static byte[] rsaEncrypt(PublicKey key, byte[] data) throws Exception {
        final Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}
