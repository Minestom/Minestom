package net.minestom.server.network.packet.client.login;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
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
import java.math.BigInteger;
import java.util.Arrays;

public class EncryptionResponsePacket implements ClientPreplayPacket {

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
                if (!loginUsername.isEmpty()) {

                    final byte[] digestedData = MojangCrypt.digestData("", MojangAuth.getKeyPair().getPublic(), getSecretKey());

                    if (digestedData == null) {
                        // Incorrect key, probably because of the client
                        MinecraftServer.LOGGER.error("Connection {} failed initializing encryption.", nettyConnection.getRemoteAddress());
                        connection.disconnect();
                        return;
                    }

                    final String string3 = new BigInteger(digestedData).toString(16);
                    final GameProfile gameProfile = MojangAuth.getSessionService().hasJoinedServer(new GameProfile(null, loginUsername), string3);
                    nettyConnection.setEncryptionKey(getSecretKey());

                    MinecraftServer.LOGGER.info("UUID of player {} is {}", loginUsername, gameProfile.getId());
                    CONNECTION_MANAGER.startPlayState(connection, gameProfile.getId(), gameProfile.getName(), true);
                }
            } catch (AuthenticationUnavailableException e) {
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

    public SecretKey getSecretKey() {
        return MojangCrypt.decryptByteToSecretKey(MojangAuth.getKeyPair().getPrivate(), sharedSecret);
    }

    public byte[] getNonce() {
        return MojangAuth.getKeyPair().getPrivate() == null ?
                this.verifyToken : MojangCrypt.decryptUsingKey(MojangAuth.getKeyPair().getPrivate(), this.verifyToken);
    }
}
