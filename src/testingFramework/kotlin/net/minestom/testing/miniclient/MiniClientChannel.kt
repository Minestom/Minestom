package net.minestom.testing.miniclient

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.minestom.server.MinecraftServer
import net.minestom.server.network.netty.packet.InboundPacket
import org.slf4j.LoggerFactory

class MiniClientChannel(private val packetProcessor: ClientSidePacketProcessor): SimpleChannelInboundHandler<InboundPacket>() {

    private val LOGGER = LoggerFactory.getLogger(MiniClientChannel::class.java)

    override fun channelRead0(ctx: ChannelHandlerContext, packet: InboundPacket) {
        try {
            packetProcessor.process(ctx, packet)
        } catch (e: Exception) {
            MinecraftServer.getExceptionManager().handleException(e)
        } finally {
            // Check remaining
            val body: ByteBuf = packet.body
            val packetId: Int = packet.packetId
            val availableBytes = body.readableBytes()
            if (availableBytes > 0) {
                val playerConnection = packetProcessor.getPlayerConnection(ctx)
                LOGGER.warn("WARNING: Packet 0x{} not fully read ({} bytes left), {}",
                        Integer.toHexString(packetId),
                        availableBytes,
                        playerConnection)
                body.skipBytes(availableBytes)
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        if (!ctx.channel().isActive) {
            return
        }
        if (MinecraftServer.shouldProcessNettyErrors()) {
            MinecraftServer.getExceptionManager().handleException(cause)
        }
        ctx.close()
    }

}
