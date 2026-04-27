package net.minestom.server.network.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Netty {@link ByteToMessageDecoder} that frames Minecraft's varint-length-prefixed
 * packets.
 *
 * <p>Minecraft sends packets as:
 * <pre>
 *   [VarInt: packet length][packet bytes …]
 * </pre>
 *
 * This decoder accumulates bytes until a full frame is available, then passes
 * the <em>payload</em> (without the length prefix) downstream.
 *
 * <p>No {@code java.nio.*} imports are used.
 */
public final class MinecraftVarintFrameDecoder extends ByteToMessageDecoder {

    /** Hard limit to guard against memory-exhaustion attacks (2 MiB). */
    private static final int MAX_PACKET_LENGTH = 1 << 21; // 2 097 152

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!ctx.channel().isActive()) return;

        in.markReaderIndex();

        // Try to read the varint length prefix
        int length = 0;
        int shift  = 0;
        for (int i = 0; i < 3; i++) {           // VarInt for length is at most 3 bytes
            if (!in.isReadable()) {
                in.resetReaderIndex();
                return;                          // wait for more data
            }
            final byte b = in.readByte();
            length |= (b & 0x7F) << shift;
            shift  += 7;
            if ((b & 0x80) == 0) {              // MSB clear -> last byte of varint
                if (in.readableBytes() < length) {
                    in.resetReaderIndex();
                    return;                      // full frame not yet available
                }
                out.add(in.readRetainedSlice(length));
                return;
            }
        }

        // Varint longer than 3 bytes  invalid
        ctx.channel().close();
        throw new IllegalStateException("VarInt length prefix too wide");
    }
}