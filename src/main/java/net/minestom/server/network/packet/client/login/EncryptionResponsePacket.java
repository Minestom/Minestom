package net.minestom.server.network.packet.client.login;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.type.array.ByteArrayData;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class EncryptionResponsePacket implements ClientPreplayPacket {

    private final static String THREAD_NAME = "Mojang Auth Thread";
    private static AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private byte[] sharedSecret;
    private byte[] verifyToken;

    @Override
    public void process(PlayerConnection connection) {
        new Thread(THREAD_NAME + " #" + UNIQUE_THREAD_ID.incrementAndGet()) {

            public void run() {
                try {
                    if (!Arrays.equals(connection.getNonce(), getNonce())) {
                        System.out.println(connection.getLoginUsername() + " tried to login with an invalid nonce!");
                        return;
                    }
                    if (!connection.getLoginUsername().isEmpty()) {
                        final String string3 = new BigInteger(MojangCrypt.digestData("", MinecraftServer.getKeyPair().getPublic(), getSecretKey())).toString(16);
                        final GameProfile gameProfile = MinecraftServer.getSessionService().hasJoinedServer(new GameProfile(null, connection.getLoginUsername()), string3);
                        ((NettyPlayerConnection) connection).setEncryptionKey(getSecretKey());
                        final int threshold = MinecraftServer.COMPRESSION_THRESHOLD;

                        if (threshold > 0) {
                            connection.enableCompression(threshold);
                        }
                        LoginSuccessPacket loginSuccessPacket = new LoginSuccessPacket(gameProfile.getId(), gameProfile.getName());
                        connection.sendPacket(loginSuccessPacket);
                        MinecraftServer.getLOGGER().info("UUID of player {} is {}", connection.getLoginUsername(), gameProfile.getId());
                        connection.setConnectionState(ConnectionState.PLAY);
                        CONNECTION_MANAGER.createPlayer(gameProfile.getId(), gameProfile.getName(), connection);
                    }
                } catch (AuthenticationUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void read(BinaryReader reader) {
        sharedSecret = ByteArrayData.decodeByteArray(reader);
        verifyToken = ByteArrayData.decodeByteArray(reader);
    }

    public SecretKey getSecretKey() {
        return MojangCrypt.decryptByteToSecretKey(MinecraftServer.getKeyPair().getPrivate(), sharedSecret);
    }

    public byte[] getNonce() {
        return MinecraftServer.getKeyPair().getPrivate() == null ? this.verifyToken : MojangCrypt.decryptUsingKey(MinecraftServer.getKeyPair().getPrivate(), this.verifyToken);
    }
}
