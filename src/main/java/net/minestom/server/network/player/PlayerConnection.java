package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.ServerPacket;

import java.net.SocketAddress;

/**
 * A PlayerConnection is an object needed for all created player
 * It can be extended to create a new kind of player (NPC for instance)
 */
public abstract class PlayerConnection {

    private ConnectionState connectionState;
    private boolean online;

    public PlayerConnection() {
        this.online = true;
        this.connectionState = ConnectionState.UNKNOWN;
    }

    public abstract void sendPacket(ByteBuf buffer);

    public abstract void writePacket(ByteBuf buffer);

    public abstract void sendPacket(ServerPacket serverPacket);

    public abstract void flush();

    public abstract SocketAddress getRemoteAddress();

    /**
     * Forcing the player to disconnect
     */
    public abstract void disconnect();

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
