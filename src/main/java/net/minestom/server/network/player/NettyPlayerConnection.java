package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import net.minestom.server.extras.mojangAuth.Decrypter;
import net.minestom.server.extras.mojangAuth.Encrypter;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.netty.codec.PacketCompressor;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.SecretKey;
import java.net.SocketAddress;

/**
 * Represents a networking connection with Netty.
 * <p>
 * It is the implementation used for all network client.
 */
public class NettyPlayerConnection extends PlayerConnection {

    private final SocketChannel channel;
    @Getter
    private boolean encrypted = false;
    @Getter
    private boolean compressed = false;

    private String serverAddress;
    private int serverPort;

    public NettyPlayerConnection(@NotNull SocketChannel channel) {
        super();
        this.channel = channel;
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
        if ((encrypted || compressed) && copy) {
            buffer = buffer.copy();
            buffer.retain();
            channel.writeAndFlush(buffer);
            buffer.release();
        } else {
            getChannel().writeAndFlush(buffer);
        }
    }

    @Override
    public void writePacket(@NotNull ByteBuf buffer, boolean copy) {
        if ((encrypted || compressed) && copy) {
            buffer = buffer.copy();
            buffer.retain();
            channel.write(buffer);
            buffer.release();
        } else {
            getChannel().write(buffer);
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
        return getChannel().remoteAddress();
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
     * Get the server address that the client used to connect.
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
     * Get the server port that the client used to connect.
     * <p>
     * WARNING: it is given by the client, it is possible for it to be wrong.
     *
     * @return the server port used
     */
    public int getServerPort() {
        return serverPort;
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
