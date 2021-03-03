package net.minestom.server.network.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.listener.manager.ServerPacketConsumer;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A PlayerConnection is an object needed for all created {@link Player}.
 * It can be extended to create a new kind of player (NPC for instance).
 */
public abstract class PlayerConnection {

    protected static final PacketListenerManager PACKET_LISTENER_MANAGER = MinecraftServer.getPacketListenerManager();

    private Player player;
    private volatile ConnectionState connectionState;
    private boolean online;

    // Text used to kick client sending too many packets
    private static final Component rateLimitKickMessage = Component.text("Too Many Packets", NamedTextColor.RED);

    //Connection Stats
    private final AtomicInteger packetCounter = new AtomicInteger(0);
    private final AtomicInteger lastPacketCounter = new AtomicInteger(0);
    private short tickCounter = 0;

    public PlayerConnection() {
        this.online = true;
        this.connectionState = ConnectionState.UNKNOWN;
    }

    /**
     * Updates values related to the network connection.
     */
    public void update() {
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
                    player.kick(rateLimitKickMessage);
                    disconnect();
                }
            }
        }
    }

    @NotNull
    public AtomicInteger getPacketCounter() {
        return packetCounter;
    }

    /**
     * Returns a printable identifier for this connection, will be the player username
     * or the connection remote address.
     *
     * @return this connection identifier
     */
    @NotNull
    public String getIdentifier() {
        final Player player = getPlayer();
        return player != null ?
                player.getUsername() :
                getRemoteAddress().toString();
    }

    /**
     * Serializes the packet and send it to the client.
     * <p>
     * Also responsible for executing {@link ConnectionManager#onPacketSend(ServerPacketConsumer)} consumers.
     *
     * @param serverPacket the packet to send
     * @see #shouldSendPacket(ServerPacket)
     */
    public abstract void sendPacket(@NotNull ServerPacket serverPacket);

    protected boolean shouldSendPacket(@NotNull ServerPacket serverPacket) {
        return player == null ||
                PACKET_LISTENER_MANAGER.processServerPacket(serverPacket, Collections.singleton(player));
    }

    /**
     * Gets the remote address of the client.
     *
     * @return the remote address
     */
    @NotNull
    public abstract SocketAddress getRemoteAddress();

    /**
     * Forcing the player to disconnect.
     */
    public abstract void disconnect();

    /**
     * Gets the player linked to this connection.
     *
     * @return the player, can be null if not initialized yet
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Changes the player linked to this connection.
     * <p>
     * WARNING: unsafe.
     *
     * @param player the player
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Gets if the client is still connected to the server.
     *
     * @return true if the player is online, false otherwise
     */
    public boolean isOnline() {
        return online;
    }

    public void refreshOnline(boolean online) {
        this.online = online;
    }

    public void setConnectionState(@NotNull ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    /**
     * Gets the client connection state.
     *
     * @return the client connection state
     */
    @NotNull
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * Gets the number of packet the client sent over the last second.
     *
     * @return the number of packet sent over the last second
     */
    public int getLastPacketCounter() {
        return lastPacketCounter.get();
    }

    @Override
    public String toString() {
        return "PlayerConnection{" +
                "connectionState=" + connectionState +
                ", identifier=" + getIdentifier() +
                '}';
    }
}
