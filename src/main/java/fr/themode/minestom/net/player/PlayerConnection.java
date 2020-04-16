package fr.themode.minestom.net.player;

import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

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
        channel.writeAndFlush(buffer.copy());
    }

    public void writePacket(ByteBuf buffer) {
        channel.write(buffer.copy());
    }

    public void sendPacket(ServerPacket serverPacket) {
        if (isOnline()) {
            ByteBuf buffer = PacketUtils.writePacket(serverPacket);
            sendPacket(buffer);
        }
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
