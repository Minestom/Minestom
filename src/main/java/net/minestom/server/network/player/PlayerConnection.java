package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.PacketUtils;

import java.net.SocketAddress;

public class PlayerConnection {

    private ChannelHandlerContext channel;
    private ConnectionState connectionState;
    private boolean online;

    public PlayerConnection(ChannelHandlerContext channel) {
        this.channel = channel;
        this.connectionState = ConnectionState.UNKNOWN;
        this.online = true;
    }

    public void sendPacket(ByteBuf buffer) {
        buffer.retain();
        channel.writeAndFlush(buffer);
    }

    public void writePacket(ByteBuf buffer) {
        buffer.retain();
        channel.write(buffer);
    }

    public void sendPacket(ServerPacket serverPacket) {
        ByteBuf buffer = PacketUtils.writePacket(serverPacket);
        sendPacket(buffer);
        buffer.release();
    }

    public void flush() {
        channel.flush();
    }

    public SocketAddress getRemoteAddress() {
        return channel.channel().remoteAddress();
    }

    public ChannelHandlerContext getChannel() {
        return channel;
    }

    public boolean isOnline() {
        return online;
    }

    public void refreshOnline(boolean online) {
        this.online = online;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }
}
