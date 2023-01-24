package net.minestom.server.network.packet.client.login;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minestom.server.MinecraftServer;
import net.minestom.server.crypto.SaltSignaturePair;
import net.minestom.server.crypto.SignatureValidator;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.InterfaceUtils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.crypto.KeyUtils;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record EncryptionResponsePacket(byte[] sharedSecret,
                                       Either<byte[], SaltSignaturePair> nonceOrSignature) implements ClientPreplayPacket {
    private static final Gson GSON = new Gson();

    public EncryptionResponsePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE_ARRAY), reader.readEither(networkBuffer -> networkBuffer.read(BYTE_ARRAY), SaltSignaturePair::new));
    }

    @Override
    public void process(@NotNull PlayerConnection connection) {
        // Encryption is only support for socket connection
        if (!(connection instanceof PlayerSocketConnection socketConnection)) return;
        AsyncUtils.runAsync(() -> {
            final String loginUsername = socketConnection.getLoginUsername();
            if (loginUsername == null || loginUsername.isEmpty()) {
                // Shouldn't happen
                return;
            }

            final boolean hasPublicKey = connection.playerPublicKey() != null;
            final boolean verificationFailed = nonceOrSignature.map(
                    nonce -> hasPublicKey || !Arrays.equals(socketConnection.getNonce(),
                            MojangCrypt.decryptUsingKey(MojangAuth.getKeyPair().getPrivate(), nonce)),
                    signature -> !hasPublicKey || !SignatureValidator
                            .from(connection.playerPublicKey().publicKey(), KeyUtils.SignatureAlgorithm.SHA256withRSA)
                            .validate(binaryWriter -> {
                                binaryWriter.write(RAW_BYTES, socketConnection.getNonce());
                                binaryWriter.write(LONG, signature.salt());
                            }, signature.signature()));

            if (verificationFailed) {
                MinecraftServer.LOGGER.error("Encryption failed for {}", loginUsername);
                return;
            }

            final byte[] digestedData = MojangCrypt.digestData("", MojangAuth.getKeyPair().getPublic(), getSecretKey());
            if (digestedData == null) {
                // Incorrect key, probably because of the client
                MinecraftServer.LOGGER.error("Connection {} failed initializing encryption.", socketConnection.getRemoteAddress());
                connection.disconnect();
                return;
            }
            // Query Mojang's session server.
            final String serverId = new BigInteger(digestedData).toString(16);
            final String username = URLEncoder.encode(loginUsername, StandardCharsets.UTF_8);

            final String url = String.format(MojangAuth.AUTH_URL, username, serverId);
            // TODO: Add ability to add ip query tag. See: https://wiki.vg/Protocol_Encryption#Authentication

            final HttpClient client = HttpClient.newHttpClient();
            final HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((response, throwable) -> {
                if (throwable != null) {
                    MinecraftServer.getExceptionManager().handleException(throwable);
                    //todo disconnect with reason
                    return;
                }
                try {
                    final JsonObject gameProfile = GSON.fromJson(response.body(), JsonObject.class);
                    if (gameProfile == null) {
                        // Invalid response
                        //todo disconnect with reason
                        return;
                    }
                    socketConnection.setEncryptionKey(getSecretKey());
                    UUID profileUUID = java.util.UUID.fromString(gameProfile.get("id").getAsString()
                            .replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                    final String profileName = gameProfile.get("name").getAsString();

                    MinecraftServer.LOGGER.info("UUID of player {} is {}", loginUsername, profileUUID);
                    CONNECTION_MANAGER.startPlayState(connection, profileUUID, profileName, true);
                } catch (Exception e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            });
        });
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE_ARRAY, sharedSecret);
        writer.writeEither(nonceOrSignature, (networkBuffer, bytes) -> networkBuffer.write(BYTE_ARRAY, bytes),
                InterfaceUtils.flipBiConsumer(SaltSignaturePair::write));
    }

    private SecretKey getSecretKey() {
        return MojangCrypt.decryptByteToSecretKey(MojangAuth.getKeyPair().getPrivate(), sharedSecret);
    }
}
