package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.ServerPacket;

import java.net.SocketAddress;

/**
 * A PlayerConnection is an object needed for all created player
 * It can be extended to create a new kind of player (NPC for instance)
 */
public abstract class PlayerConnection {

    private Player player;
    //Could be null. Only used for Mojang Auth
    @Getter @Setter private String loginUsername;
    //Could be null. Only used for Mojang Auth
    @Getter @Setter private byte[] nonce = new byte[4];
    private ConnectionState connectionState;
    private boolean online;

    public PlayerConnection() {
        this.online = true;
        this.connectionState = ConnectionState.UNKNOWN;
    }

    public abstract void enableCompression(int threshold);
    /**
     *
     * @param buffer The buffer to send.
     * @param copy Should be true unless your only using the ByteBuf once.
     */
    public abstract void sendPacket(ByteBuf buffer, boolean copy);

    /**
     *
     * @param buffer The buffer to send.
     * @param copy Should be true unless your only using the ByteBuf once.
     */
    public abstract void writePacket(ByteBuf buffer, boolean copy);

    public abstract void sendPacket(ServerPacket serverPacket);

    public abstract void flush();

    public abstract SocketAddress getRemoteAddress();

    /**
     * Forcing the player to disconnect
     */
    public abstract void disconnect();

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
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
