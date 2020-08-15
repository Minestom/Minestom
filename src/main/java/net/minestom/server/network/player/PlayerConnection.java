package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.login.LoginDisconnect;
import net.minestom.server.network.packet.server.play.DisconnectPacket;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A PlayerConnection is an object needed for all created player
 * It can be extended to create a new kind of player (NPC for instance)
 */
public abstract class PlayerConnection {

    private Player player;
    //Could be null. Only used for Mojang Auth
    @Getter
    @Setter
    private String loginUsername;
    //Could be null. Only used for Mojang Auth
    @Getter
    @Setter
    private byte[] nonce = new byte[4];
    private ConnectionState connectionState;
    private boolean online;

    // Text used to kick client sending too many packets
    private static final ColoredText rateLimitKickMessage = ColoredText.of(ChatColor.RED + "Too Many Packets");

    //Connection Stats
    @Getter
    private final AtomicInteger packetCounter = new AtomicInteger(0);
    private final AtomicInteger lastPacketCounter = new AtomicInteger(0);
    private short tickCounter = 0;

    public PlayerConnection() {
        this.online = true;
        this.connectionState = ConnectionState.UNKNOWN;
    }

    /**
     * Update values related to the network connection
     */
    public void updateStats() {
        // Check rate limit
        if (MinecraftServer.getRateLimit() > 0) {
            tickCounter++;
            if (tickCounter % MinecraftServer.TICK_PER_SECOND == 0 && tickCounter > 0) {
                tickCounter = 0;
                // Retrieve the packet count
                final int count = packetCounter.get();
                this.lastPacketCounter.set(count);
                this.packetCounter.set(0);
                if (count > MinecraftServer.getRateLimit()) {
                    // Sent too many packets
                    if (connectionState == ConnectionState.LOGIN) {
                        sendPacket(new LoginDisconnect("Too Many Packets"));
                    } else {
                        DisconnectPacket disconnectPacket = new DisconnectPacket();
                        disconnectPacket.message = rateLimitKickMessage;
                        sendPacket(disconnectPacket);
                    }
                    disconnect();
                    refreshOnline(false);
                }
            }
        }
    }

    public abstract void enableCompression(int threshold);

    /**
     * Send a raw {@link ByteBuf} to the client
     *
     * @param buffer The buffer to send.
     * @param copy   Should be true unless your only using the ByteBuf once.
     */
    public abstract void sendPacket(ByteBuf buffer, boolean copy);

    /**
     * Write a raw {@link ByteBuf} to the client
     *
     * @param buffer The buffer to send.
     * @param copy   Should be true unless your only using the ByteBuf once.
     */
    public abstract void writePacket(ByteBuf buffer, boolean copy);

    /**
     * Serialize the packet and send it to the client
     *
     * @param serverPacket the packet to send
     */
    public abstract void sendPacket(ServerPacket serverPacket);

    /**
     * Flush all waiting packets
     */
    public abstract void flush();

    /**
     * Get the remote address of the client
     *
     * @return the remote address
     */
    public abstract SocketAddress getRemoteAddress();

    /**
     * Forcing the player to disconnect
     */
    public abstract void disconnect();

    /**
     * Get the player linked to this connection
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Change the player linked to this connection
     * <p>
     * WARNING: unsafe
     *
     * @param player the player
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Get if the client is still connected to the server
     *
     * @return true if the player is online, false otherwise
     */
    public boolean isOnline() {
        return online;
    }

    public void refreshOnline(boolean online) {
        this.online = online;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    /**
     * Get the client connection state
     *
     * @return the client connection state
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * Get the number of packet the client sent over the last second
     *
     * @return the number of packet sent over the last second
     */
    public int getLastPacketCounter() {
        return lastPacketCounter.get();
    }
}
