package net.minestom.server.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.netty.packet.InboundPacket;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.client.handler.ClientLoginPacketsHandler;
import net.minestom.server.network.packet.client.handler.ClientPlayPacketsHandler;
import net.minestom.server.network.packet.client.handler.ClientStatusPacketsHandler;
import net.minestom.server.network.packet.client.handshake.HandshakePacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.Readable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for processing client packets.
 * <p>
 * You can retrieve the different packet handlers per state (status/login/play)
 * from the {@link net.minestom.server.network.packet.client.handler.ClientPacketsHandler} class.
 * <p>
 * Packet handlers are cached here and can be retrieved with {@link #getStatusPacketsHandler()}, {@link #getLoginPacketsHandler()}
 * and {@link #getPlayPacketsHandler()}. The one to use depend on the type of packet you need to retrieve (the packet id 0 does not have
 * the same meaning as it is a login or play packet).
 */
public final class PacketProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(PacketProcessor.class);

    private final Map<ChannelHandlerContext, PlayerConnection> connectionPlayerConnectionMap = new ConcurrentHashMap<>();

    // Protocols state
    private final ClientStatusPacketsHandler statusPacketsHandler;
    private final ClientLoginPacketsHandler loginPacketsHandler;
    private final ClientPlayPacketsHandler playPacketsHandler;

    public PacketProcessor() {
        this.statusPacketsHandler = new ClientStatusPacketsHandler();
        this.loginPacketsHandler = new ClientLoginPacketsHandler();
        this.playPacketsHandler = new ClientPlayPacketsHandler();
    }

    public void process(@NotNull ChannelHandlerContext context, @NotNull InboundPacket packet) {
        final SocketChannel socketChannel = (SocketChannel) context.channel();

        // Create the netty player connection object if not existing
        PlayerConnection playerConnection = connectionPlayerConnectionMap.get(context);
        if (playerConnection == null) {
            // Should never happen
            context.close();
            return;
        }

        // Prevent the client from sending packets when disconnected (kick)
        if (!playerConnection.isOnline() || !socketChannel.isActive()) {
            playerConnection.disconnect();
            return;
        }

        // Increment packet count (checked in PlayerConnection#update)
        if (MinecraftServer.getRateLimit() > 0) {
            playerConnection.getPacketCounter().incrementAndGet();
        }

        final ConnectionState connectionState = playerConnection.getConnectionState();

        final int packetId = packet.getPacketId();
        BinaryReader binaryReader = new BinaryReader(packet.getBody());

        if (connectionState == ConnectionState.UNKNOWN) {
            // Should be handshake packet
            if (packetId == 0) {
                HandshakePacket handshakePacket = new HandshakePacket();
                safeRead(playerConnection, handshakePacket, binaryReader);
                handshakePacket.process(playerConnection);
            }
            return;
        }

        switch (connectionState) {
            case PLAY:
                final Player player = playerConnection.getPlayer();
                ClientPlayPacket playPacket = (ClientPlayPacket) playPacketsHandler.getPacketInstance(packetId);
                safeRead(playerConnection, playPacket, binaryReader);
                assert player != null;
                player.addPacketToQueue(playPacket);
                break;
            case LOGIN:
                final ClientPreplayPacket loginPacket = (ClientPreplayPacket) loginPacketsHandler.getPacketInstance(packetId);
                safeRead(playerConnection, loginPacket, binaryReader);
                loginPacket.process(playerConnection);
                break;
            case STATUS:
                final ClientPreplayPacket statusPacket = (ClientPreplayPacket) statusPacketsHandler.getPacketInstance(packetId);
                safeRead(playerConnection, statusPacket, binaryReader);
                statusPacket.process(playerConnection);
                break;
        }
    }

    /**
     * Retrieves a player connection from its channel.
     *
     * @param context the connection context
     * @return the connection of this channel, null if not found
     */
    @Nullable
    public PlayerConnection getPlayerConnection(ChannelHandlerContext context) {
        return connectionPlayerConnectionMap.get(context);
    }

    public void createPlayerConnection(@NotNull ChannelHandlerContext context) {
        final PlayerConnection playerConnection = new NettyPlayerConnection((SocketChannel) context.channel());
        connectionPlayerConnectionMap.put(context, playerConnection);
    }

    public PlayerConnection removePlayerConnection(@NotNull ChannelHandlerContext context) {
        return connectionPlayerConnectionMap.remove(context);
    }

    /**
     * Gets the handler for client status packets.
     *
     * @return the status packets handler
     * @see <a href="https://wiki.vg/Protocol#Status">Status packets</a>
     */
    @NotNull
    public ClientStatusPacketsHandler getStatusPacketsHandler() {
        return statusPacketsHandler;
    }

    /**
     * Gets the handler for client login packets.
     *
     * @return the status login handler
     * @see <a href="https://wiki.vg/Protocol#Login">Login packets</a>
     */
    @NotNull
    public ClientLoginPacketsHandler getLoginPacketsHandler() {
        return loginPacketsHandler;
    }

    /**
     * Gets the handler for client play packets.
     *
     * @return the play packets handler
     * @see <a href="https://wiki.vg/Protocol#Play">Play packets</a>
     */
    @NotNull
    public ClientPlayPacketsHandler getPlayPacketsHandler() {
        return playPacketsHandler;
    }

    /**
     * Calls {@link Readable#read(BinaryReader)} and catch all the exceptions to be printed using the packet processor logger.
     *
     * @param connection the connection who sent the packet
     * @param readable   the readable interface
     * @param reader     the buffer containing the packet
     */
    private void safeRead(@NotNull PlayerConnection connection, @NotNull Readable readable, @NotNull BinaryReader reader) {
        final int readableBytes = reader.available();

        // Check if there is anything to read
        if (readableBytes == 0) {
            return;
        }

        try {
            readable.read(reader);
        } catch (Exception e) {
            final Player player = connection.getPlayer();
            final String username = player != null ? player.getUsername() : "null";
            LOGGER.warn("Connection {} ({}) sent an unexpected packet.",
                    connection.getRemoteAddress(),
                    username);
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }
}
