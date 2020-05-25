package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.PacketUtils;

import java.net.SocketAddress;

/**
 * Represent a networking connection with Netty
 * It is the implementation used for all server connection client
 */
public class BasicPlayerConnection extends PlayerConnection {

    private ChannelHandlerContext channel;

    public BasicPlayerConnection(ChannelHandlerContext channel) {
        super();
        this.channel = channel;
    }

    @Override
    public void sendPacket(ByteBuf buffer) {
        buffer.retain();
        getChannel().writeAndFlush(buffer);
    }

    @Override
    public void writePacket(ByteBuf buffer) {
        buffer.retain();
        getChannel().write(buffer);
    }

    @Override
    public void sendPacket(ServerPacket serverPacket) {
        ByteBuf buffer = PacketUtils.writePacket(serverPacket);
        sendPacket(buffer);
        buffer.release();
    }

    @Override
    public void flush() {
        getChannel().flush();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return getChannel().channel().remoteAddress();
    }

    @Override
    public void disconnect() {
        getChannel().close();
    }

    public ChannelHandlerContext getChannel() {
        return channel;
    }

}
