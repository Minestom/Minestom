package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import net.minestom.server.network.netty.codec.PacketCompressor;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;

import java.net.SocketAddress;

/**
 * Represent a networking connection with Netty
 * It is the implementation used for all server connection client
 */
public class NettyPlayerConnection extends PlayerConnection {

    private final SocketChannel channel;

    public NettyPlayerConnection(SocketChannel channel) {
        super();

        this.channel = channel;
    }

    @Override
    public void enableCompression(int threshold) {
        sendPacket(new SetCompressionPacket(threshold));

        channel.pipeline().addAfter("framer", "compressor", new PacketCompressor(threshold));
    }

    @Override
    public void sendPacket(ByteBuf buffer) {
        buffer.retain();
        channel.writeAndFlush(buffer);
    }

    @Override
    public void writePacket(ByteBuf buffer) {
        buffer.retain();
        channel.write(buffer);
    }

    @Override
    public void sendPacket(ServerPacket serverPacket) {
        channel.writeAndFlush(serverPacket);
    }

    @Override
    public void flush() {
        channel.flush();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public void disconnect() {
        getChannel().close();
    }

    public Channel getChannel() {
        return channel;
    }

}
