package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.extras.mojangAuth.Decrypter;
import net.minestom.server.extras.mojangAuth.Encrypter;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.netty.codec.PacketCompressor;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.SecretKey;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a networking connection with Netty.
 * <p>
 * It is the implementation used for all network client.
 */
public class NettyPlayerConnection extends PlayerConnection {

    private final SocketChannel channel;

    private SocketAddress remoteAddress;
    @Getter
    private boolean encrypted = false;
    @Getter
    private boolean compressed = false;

    //Could be null. Only used for Mojang Auth
    @Getter
    @Setter
    private byte[] nonce = new byte[4];

    private String loginUsername;
    private String serverAddress;
    private int serverPort;

    // Used for the login plugin request packet, to retrieve the channel from a message id,
    // cleared once the player enters the play state
    private final Map<Integer, String> pluginRequestMap = new ConcurrentHashMap<>();

    public NettyPlayerConnection(@NotNull SocketChannel channel) {
        super();
        this.channel = channel;
        this.remoteAddress = channel.remoteAddress();
    }

    /**
     * Sets the encryption key and add the channels to the pipeline.
     *
     * @param secretKey the secret key to use in the encryption
     * @throws IllegalStateException if encryption is already enabled for this connection
     */
    public void setEncryptionKey(@NotNull SecretKey secretKey) {
        Check.stateCondition(encrypted, "Encryption is already enabled!");
        this.encrypted = true;
        getChannel().pipeline().addBefore("framer", "decrypt", new Decrypter(MojangCrypt.getCipher(2, secretKey)));
        getChannel().pipeline().addBefore("framer", "encrypt", new Encrypter(MojangCrypt.getCipher(1, secretKey)));
    }

    /**
     * Enables compression and add a new channel to the pipeline.
     *
     * @param threshold the threshold for a packet to be compressible
     * @throws IllegalStateException if encryption is already enabled for this connection
     */
    public void enableCompression(int threshold) {
        Check.stateCondition(compressed, "Compression is already enabled!");
        this.compressed = true;
        sendPacket(new SetCompressionPacket(threshold));
        channel.pipeline().addAfter("framer", "compressor", new PacketCompressor(threshold));
    }

    @Override
    public void sendPacket(@NotNull ByteBuf buffer, boolean copy) {
        if (copy) {
            buffer = buffer.copy();
            buffer.retain();
            channel.writeAndFlush(buffer);
            buffer.release();
        } else {
            channel.writeAndFlush(buffer);
        }
    }

    @Override
    public void writePacket(@NotNull ByteBuf buffer, boolean copy) {
        if (copy) {
            buffer = buffer.copy();
            buffer.retain();
            channel.write(buffer);
            buffer.release();
        } else {
            channel.write(buffer);
        }
    }

    @Override
    public void sendPacket(@NotNull ServerPacket serverPacket) {
        channel.writeAndFlush(serverPacket);
    }

    @Override
    public void flush() {
        getChannel().flush();
    }

    @NotNull
    @Override
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Changes the internal remote address field.
     * <p>
     * Mostly unsafe, used internally when interacting with a proxy.
     *
     * @param remoteAddress the new connection remote address
     */
    public void setRemoteAddress(@NotNull SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void disconnect() {
        getChannel().close();
    }

    @NotNull
    public Channel getChannel() {
        return channel;
    }

    /**
     * Retrieves the username received from the client during connection.
     * <p>
     * This value has not been checked and could be anything.
     *
     * @return the username given by the client, unchecked
     */
    @Nullable
    public String getLoginUsername() {
        return loginUsername;
    }

    /**
     * Sets the internal login username field.
     *
     * @param loginUsername the new login username field
     */
    public void UNSAFE_setLoginUsername(@NotNull String loginUsername) {
        this.loginUsername = loginUsername;
    }

    /**
     * Gets the server address that the client used to connect.
     * <p>
     * WARNING: it is given by the client, it is possible for it to be wrong.
     *
     * @return the server address used
     */
    @Nullable
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Gets the server port that the client used to connect.
     * <p>
     * WARNING: it is given by the client, it is possible for it to be wrong.
     *
     * @return the server port used
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Adds an entry to the plugin request map.
     * <p>
     * Only working if {@link #getConnectionState()} is {@link net.minestom.server.network.ConnectionState#LOGIN}.
     *
     * @param messageId the message id
     * @param channel   the packet channel
     * @throws IllegalStateException if a messageId with the value {@code messageId} already exists for this connection
     */
    public void addPluginRequestEntry(int messageId, @NotNull String channel) {
        if (!getConnectionState().equals(ConnectionState.LOGIN)) {
            return;
        }
        Check.stateCondition(pluginRequestMap.containsKey(messageId), "You cannot have two messageId with the same value");
        this.pluginRequestMap.put(messageId, channel);
    }

    /**
     * Gets a request channel from a message id, previously cached using {@link #addPluginRequestEntry(int, String)}.
     * <p>
     * Be aware that the internal map is cleared once the player enters the play state.
     *
     * @param messageId the message id
     * @return the channel linked to the message id, null if not found
     */
    @Nullable
    public String getPluginRequestChannel(int messageId) {
        return pluginRequestMap.get(messageId);
    }

    @Override
    public void setConnectionState(@NotNull ConnectionState connectionState) {
        super.setConnectionState(connectionState);
        // Clear the plugin request map (since it is not used anymore)
        if (connectionState.equals(ConnectionState.PLAY)) {
            this.pluginRequestMap.clear();
        }
    }

    /**
     * Used in {@link net.minestom.server.network.packet.client.handshake.HandshakePacket} to change the internal fields.
     *
     * @param serverAddress the server address which the client used
     * @param serverPort    the server port which the client used
     */
    public void refreshServerInformation(@Nullable String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }
}
