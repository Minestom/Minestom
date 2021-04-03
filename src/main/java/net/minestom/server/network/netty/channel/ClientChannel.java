package net.minestom.server.network.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.netty.packet.InboundPacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientChannel extends SimpleChannelInboundHandler<InboundPacket> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientChannel.class);

    private final static ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();
    private final PacketProcessor packetProcessor;

    public ClientChannel(@NotNull PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        //System.out.println("CONNECTION");
        packetProcessor.createPlayerConnection(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, InboundPacket packet) {
        try {
            packetProcessor.process(ctx, packet);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
        } finally {
            // Check remaining
            final ByteBuf body = packet.getBody();
            final int packetId = packet.getPacketId();

            final int availableBytes = body.readableBytes();

            if (availableBytes > 0) {
                final PlayerConnection playerConnection = packetProcessor.getPlayerConnection(ctx);

                LOGGER.warn("WARNING: Packet 0x{} not fully read ({} bytes left), {}",
                        Integer.toHexString(packetId),
                        availableBytes,
                        playerConnection);

                body.skipBytes(availableBytes);
            }
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        PlayerConnection playerConnection = packetProcessor.removePlayerConnection(ctx);
        if (playerConnection != null) {
            // Remove the connection
            playerConnection.refreshOnline(false);
            Player player = playerConnection.getPlayer();
            if (player != null) {
                player.remove();
                CONNECTION_MANAGER.removePlayer(playerConnection);
            }

            // Release tick buffer
            if (playerConnection instanceof NettyPlayerConnection) {
                final ByteBuf tickBuffer = ((NettyPlayerConnection) playerConnection).getTickBuffer();
                synchronized (tickBuffer) {
                    tickBuffer.release();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!ctx.channel().isActive()) {
            return;
        }

        if (MinecraftServer.shouldProcessNettyErrors()) {
            MinecraftServer.getExceptionManager().handleException(cause);
        }
        ctx.close();
    }
}
