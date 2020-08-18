package net.minestom.server.network.netty.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.netty.packet.InboundPacket;
import net.minestom.server.network.player.PlayerConnection;

@Slf4j
public class ClientChannel extends SimpleChannelInboundHandler<InboundPacket> {

    private final ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
    private final PacketProcessor packetProcessor;

    public ClientChannel(PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //System.out.println("CONNECTION");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, InboundPacket packet) {
        try {
            packetProcessor.process(ctx, packet);
        } finally {
            final int availableBytes = packet.body.readableBytes();

            if (availableBytes > 0) {
                // TODO log4j2
                System.out.println("Packet 0x" + Integer.toHexString(packet.packetId)
                        + " not fully read (" + availableBytes + " bytes left)");

                packet.body.skipBytes(availableBytes);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
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
        log.info(cause.getMessage());
        ctx.close();
    }
}
