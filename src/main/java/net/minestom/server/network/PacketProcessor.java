package net.minestom.server.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.netty.packet.InboundPacket;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.client.handler.ClientLoginPacketsHandler;
import net.minestom.server.network.packet.client.handler.ClientPlayPacketsHandler;
import net.minestom.server.network.packet.client.handler.ClientStatusPacketsHandler;
import net.minestom.server.network.packet.client.handshake.HandshakePacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketProcessor {

    private Map<ChannelHandlerContext, PlayerConnection> connectionPlayerConnectionMap = new ConcurrentHashMap<>();

    private ConnectionManager connectionManager;

    // Protocols
    private ClientStatusPacketsHandler statusPacketsHandler;
    private ClientLoginPacketsHandler loginPacketsHandler;
    private ClientPlayPacketsHandler playPacketsHandler;

    public PacketProcessor() {
        this.connectionManager = MinecraftServer.getConnectionManager();

        this.statusPacketsHandler = new ClientStatusPacketsHandler();
        this.loginPacketsHandler = new ClientLoginPacketsHandler();
        this.playPacketsHandler = new ClientPlayPacketsHandler();
    }

    private List<Integer> printBlackList = Arrays.asList(17, 18, 19);

    public void process(ChannelHandlerContext channel, InboundPacket packet) {
        PlayerConnection playerConnection = connectionPlayerConnectionMap.computeIfAbsent(
                channel, c -> new NettyPlayerConnection((SocketChannel) channel.channel())
        );

        final ConnectionState connectionState = playerConnection.getConnectionState();

        //if (!printBlackList.contains(id)) {
        //System.out.println("RECEIVED ID: 0x" + Integer.toHexString(id) + " State: " + connectionState);
        //}

        PacketReader packetReader = new PacketReader(packet.body);

        if (connectionState == ConnectionState.UNKNOWN) {
            // Should be handshake packet
            if (packet.packetId == 0) {
                HandshakePacket handshakePacket = new HandshakePacket();
                handshakePacket.read(packetReader);
                handshakePacket.process(playerConnection, connectionManager);
            }
            return;
        }

        switch (connectionState) {
            case PLAY:
                final Player player = playerConnection.getPlayer();
                ClientPlayPacket playPacket = (ClientPlayPacket) playPacketsHandler.getPacketInstance(packet.packetId);
                playPacket.read(packetReader);
                player.addPacketToQueue(playPacket);
                break;
            case LOGIN:
                final ClientPreplayPacket loginPacket = (ClientPreplayPacket) loginPacketsHandler.getPacketInstance(packet.packetId);
                loginPacket.read(packetReader);
                loginPacket.process(playerConnection, connectionManager);
                break;
            case STATUS:
                final ClientPreplayPacket statusPacket = (ClientPreplayPacket) statusPacketsHandler.getPacketInstance(packet.packetId);
                statusPacket.read(packetReader);

                statusPacket.process(playerConnection, connectionManager);
                break;
        }
    }

    public PlayerConnection getPlayerConnection(ChannelHandlerContext channel) {
        return connectionPlayerConnectionMap.get(channel);
    }

    public void removePlayerConnection(ChannelHandlerContext channel) {
        connectionPlayerConnectionMap.remove(channel);
    }
}
