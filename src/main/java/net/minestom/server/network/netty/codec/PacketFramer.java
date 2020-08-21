package net.minestom.server.network.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.CorruptedFrameException;
import net.minestom.server.utils.Utils;

import java.util.List;

public class PacketFramer extends ByteToMessageCodec<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf from, ByteBuf to) {
        final int packetSize = from.readableBytes();
        final int headerSize = Utils.getVarIntSize(packetSize);

        if (headerSize > 3) {
            throw new IllegalStateException("Unable to fit " + headerSize + " into 3");
        }

        to.ensureWritable(packetSize + headerSize);

        Utils.writeVarIntBuf(to, packetSize);
        to.writeBytes(from, from.readerIndex(), packetSize);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
        buf.markReaderIndex();

        for (int i = 0; i < 3; ++i) {
            if (!buf.isReadable()) {
                buf.resetReaderIndex();
                return;
            }

            final byte b = buf.readByte();

            if (b >= 0) {
                buf.resetReaderIndex();

                final int j = Utils.readVarInt(buf);

                if (buf.readableBytes() < j) {
                    buf.resetReaderIndex();
                    return;
                }

                out.add(buf.readRetainedSlice(j));
                return;
            }
        }

        throw new CorruptedFrameException("length wider than 21-bit");
    }
}
