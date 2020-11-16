package net.minestom.server.network.netty.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.netty.packet.InboundPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientChannel extends SimpleChannelInboundHandler<InboundPacket> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientChannel.class);

    private final ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
    private final PacketProcessor packetProcessor;

    public ClientChannel(@NotNull PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        //System.out.println("CONNECTION");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, InboundPacket packet) {
        try {
            packetProcessor.process(ctx, packet);
        } finally {
            final int availableBytes = packet.body.readableBytes();

            if (availableBytes > 0) {
                final PlayerConnection playerConnection = packetProcessor.getPlayerConnection(ctx);

                LOGGER.warn("WARNING: Packet 0x" + Integer.toHexString(packet.packetId)
                        + " not fully read (" + availableBytes + " bytes left), " + playerConnection);

                packet.body.skipBytes(availableBytes);
            }
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        PlayerConnection playerConnection = packetProcessor.getPlayerConnection(ctx);
        if (playerConnection != null) {
            // Remove the connection
            playerConnection.refreshOnline(false);
            Player player = playerConnection.getPlayer();
            if (player != null) {
                player.remove();
                connectionManager.removePlayer(playerConnection);
            }
            packetProcessor.removePlayerConnection(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.info(cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
