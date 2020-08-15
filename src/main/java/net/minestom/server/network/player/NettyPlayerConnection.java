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

import javax.crypto.SecretKey;
import java.net.SocketAddress;

/**
 * Represent a networking connection with Netty
 * It is the implementation used for all server connection client
 */
public class NettyPlayerConnection extends PlayerConnection {

    private final SocketChannel channel;
    @Getter
    private boolean encrypted = false;
    @Getter
    private boolean compressed = false;

    public NettyPlayerConnection(SocketChannel channel) {
        super();
        this.channel = channel;
    }

    public void setEncryptionKey(SecretKey secretKey) {
        this.encrypted = true;
        getChannel().pipeline().addBefore("framer", "decrypt", new Decrypter(MojangCrypt.getCipher(2, secretKey)));
        getChannel().pipeline().addBefore("framer", "encrypt", new Encrypter(MojangCrypt.getCipher(1, secretKey)));
    }

    @Override
    public void enableCompression(int threshold) {
        this.compressed = true;
        sendPacket(new SetCompressionPacket(threshold));
        channel.pipeline().addAfter("framer", "compressor", new PacketCompressor(threshold));
    }

    @Override
    public void sendPacket(ByteBuf buffer, boolean copy) {
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
    public void writePacket(ByteBuf buffer, boolean copy) {
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
    public void sendPacket(ServerPacket serverPacket) {
        channel.writeAndFlush(serverPacket);
    }

    @Override
    public void flush() {
        getChannel().flush();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return getChannel().remoteAddress();
    }

    @Override
    public void disconnect() {
        getChannel().close();
    }

    public Channel getChannel() {
        return channel;
    }

}
