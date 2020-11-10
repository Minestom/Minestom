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
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketProcessor {

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

    public void process(ChannelHandlerContext channel, InboundPacket packet) {
        // Create the netty player connection object if not existing
        PlayerConnection playerConnection = connectionPlayerConnectionMap.computeIfAbsent(
                channel, c -> new NettyPlayerConnection((SocketChannel) channel.channel())
        );

        if (MinecraftServer.getRateLimit() > 0)
            playerConnection.getPacketCounter().incrementAndGet();

        final ConnectionState connectionState = playerConnection.getConnectionState();

        BinaryReader binaryReader = new BinaryReader(packet.body);

        if (connectionState == ConnectionState.UNKNOWN) {
            // Should be handshake packet
            if (packet.packetId == 0) {
                HandshakePacket handshakePacket = new HandshakePacket();
                handshakePacket.read(binaryReader);
                handshakePacket.process(playerConnection);
            }
            return;
        }

        switch (connectionState) {
            case PLAY:
                final Player player = playerConnection.getPlayer();
                ClientPlayPacket playPacket = (ClientPlayPacket) playPacketsHandler.getPacketInstance(packet.packetId);
                playPacket.read(binaryReader);
                player.addPacketToQueue(playPacket);
                break;
            case LOGIN:
                final ClientPreplayPacket loginPacket = (ClientPreplayPacket) loginPacketsHandler.getPacketInstance(packet.packetId);
                loginPacket.read(binaryReader);
                loginPacket.process(playerConnection);
                break;
            case STATUS:
                final ClientPreplayPacket statusPacket = (ClientPreplayPacket) statusPacketsHandler.getPacketInstance(packet.packetId);
                statusPacket.read(binaryReader);
                statusPacket.process(playerConnection);
                break;
        }
    }

    /**
     * Retrieves a player connection from its channel.
     *
     * @param channel the connection channel
     * @return the connection of this channel, null if not found
     */
    @Nullable
    public PlayerConnection getPlayerConnection(ChannelHandlerContext channel) {
        return connectionPlayerConnectionMap.get(channel);
    }

    public void removePlayerConnection(ChannelHandlerContext channel) {
        connectionPlayerConnectionMap.remove(channel);
    }
}
