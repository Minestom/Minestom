package net.minestom.server.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.netty.packet.InboundPacket;
import net.minestom.server.network.packet.Packet;
import net.minestom.server.network.packet.client.handler.ClientLoginPacketsHandler;
import net.minestom.server.network.packet.client.handler.ClientPlayPacketsHandler;
import net.minestom.server.network.packet.client.handshake.HandshakePacket;
import net.minestom.server.network.packet.handler.PacketsHandler;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.Readable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PacketProcessor<PlayPacket extends Packet, PreplayPacket extends Packet> {

    private final static Logger LOGGER = LoggerFactory.getLogger(PacketProcessor.class);

    private PacketsHandler<PlayPacket> playPacketsHandler;
    private PacketsHandler<PreplayPacket> loginPacketsHandler;
    private PacketsHandler<PreplayPacket> statusPacketsHandler;

    public PacketProcessor() {
        this.playPacketsHandler = createPlayPacketsHandler();
        this.loginPacketsHandler = createLoginPacketsHandler();
        this.statusPacketsHandler = createStatusPacketsHandler();
    }

    protected abstract PacketsHandler<PlayPacket> createPlayPacketsHandler();
    protected abstract PacketsHandler<PreplayPacket> createLoginPacketsHandler();
    protected abstract PacketsHandler<PreplayPacket> createStatusPacketsHandler();

    public void process(@NotNull ChannelHandlerContext context, @NotNull InboundPacket packet) {
        final SocketChannel socketChannel = (SocketChannel) context.channel();

        // Create the netty player connection object if not existing
        PlayerConnection playerConnection = getPlayerConnection(context);
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

        if (connectionState == ConnectionState.HANDSHAKE) {
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
                PlayPacket playPacket = (PlayPacket) playPacketsHandler.getPacketInstance(packetId);
                safeRead(playerConnection, playPacket, binaryReader);
                processPlayPacket(playerConnection, playPacket);
                break;
            case LOGIN:
                final PreplayPacket loginPacket = (PreplayPacket) loginPacketsHandler.getPacketInstance(packetId);
                safeRead(playerConnection, loginPacket, binaryReader);
                processLoginPacket(playerConnection, loginPacket);
                break;
            case STATUS:
                final PreplayPacket statusPacket = (PreplayPacket) statusPacketsHandler.getPacketInstance(packetId);
                safeRead(playerConnection, statusPacket, binaryReader);
                processStatusPacket(playerConnection, statusPacket);
                break;
        }
    }

    protected abstract void processPlayPacket(PlayerConnection playerConnection, PlayPacket playPacket);

    protected abstract void processLoginPacket(PlayerConnection playerConnection, PreplayPacket statusPacket);

    protected abstract void processStatusPacket(PlayerConnection playerConnection, PreplayPacket statusPacket);

    /**
     * Retrieves a player connection from its channel.
     *
     * @param context the connection context
     * @return the connection of this channel, null if not found
     */
    @Nullable
    public abstract PlayerConnection getPlayerConnection(ChannelHandlerContext context);

    /**
     * Gets the handler for client status packets.
     *
     * @return the status packets handler
     * @see <a href="https://wiki.vg/Protocol#Status">Status packets</a>
     */
    @NotNull
    public PacketsHandler<PreplayPacket> getStatusPacketsHandler() {
        return statusPacketsHandler;
    }

    /**
     * Gets the handler for client login packets.
     *
     * @return the status login handler
     * @see <a href="https://wiki.vg/Protocol#Login">Login packets</a>
     */
    @NotNull
    public PacketsHandler<PreplayPacket> getLoginPacketsHandler() {
        return loginPacketsHandler;
    }

    /**
     * Gets the handler for client play packets.
     *
     * @return the play packets handler
     * @see <a href="https://wiki.vg/Protocol#Play">Play packets</a>
     */
    @NotNull
    public PacketsHandler<PlayPacket> getPlayPacketsHandler() {
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
