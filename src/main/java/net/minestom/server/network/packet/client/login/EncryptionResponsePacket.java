package net.minestom.server.network.packet.client.login;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.type.array.ByteArrayData;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

public class EncryptionResponsePacket implements ClientPreplayPacket {
    private static final Gson GSON = new Gson();
    private byte[] sharedSecret;
    private byte[] verifyToken;

    public EncryptionResponsePacket() {
        sharedSecret = new byte[0];
        verifyToken = new byte[0];
    }

    @Override
    public void process(@NotNull PlayerConnection connection) {

        // Encryption is only support for netty connection
        if (!(connection instanceof NettyPlayerConnection)) {
            return;
        }
        final NettyPlayerConnection nettyConnection = (NettyPlayerConnection) connection;

        AsyncUtils.runAsync(() -> {
            try {
                final String loginUsername = nettyConnection.getLoginUsername();
                if (!Arrays.equals(nettyConnection.getNonce(), getNonce())) {
                    MinecraftServer.LOGGER.error("{} tried to login with an invalid nonce!", loginUsername);
                    return;
                }
                if (loginUsername != null && !loginUsername.isEmpty()) {

                    final byte[] digestedData = MojangCrypt.digestData("", MojangAuth.getKeyPair().getPublic(), getSecretKey());

                    if (digestedData == null) {
                        // Incorrect key, probably because of the client
                        MinecraftServer.LOGGER.error("Connection {} failed initializing encryption.", nettyConnection.getRemoteAddress());
                        connection.disconnect();
                        return;
                    }

                    // Query Mojang's sessionserver.
                    final String serverId = new BigInteger(digestedData).toString(16);
                    InputStream gameProfileStream = new URL(
                            "https://sessionserver.mojang.com/session/minecraft/hasJoined?"
                                    + "username=" + loginUsername + "&"
                                    + "serverId=" + serverId
                            // TODO: Add ability to add ip query tag. See: https://wiki.vg/Protocol_Encryption#Authentication
                    ).openStream();

                    final JsonObject gameProfile = GSON.fromJson(new InputStreamReader(gameProfileStream), JsonObject.class);
                    nettyConnection.setEncryptionKey(getSecretKey());
                    UUID profileUUID = UUID.fromString(gameProfile.get("id").getAsString().replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                    String profileName = gameProfile.get("name").getAsString();

                    MinecraftServer.LOGGER.info("UUID of player {} is {}", loginUsername, profileUUID);
                    CONNECTION_MANAGER.startPlayState(connection, profileUUID, profileName, true);
                }
            } catch (IOException e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        });
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        sharedSecret = ByteArrayData.decodeByteArray(reader);
        verifyToken = ByteArrayData.decodeByteArray(reader);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        ByteArrayData.encodeByteArray(writer, sharedSecret);
        ByteArrayData.encodeByteArray(writer, verifyToken);
    }

    private SecretKey getSecretKey() {
        return MojangCrypt.decryptByteToSecretKey(MojangAuth.getKeyPair().getPrivate(), sharedSecret);
    }

    private byte[] getNonce() {
        return MojangAuth.getKeyPair().getPrivate() == null ?
                this.verifyToken : MojangCrypt.decryptUsingKey(MojangAuth.getKeyPair().getPrivate(), this.verifyToken);
    }
}
