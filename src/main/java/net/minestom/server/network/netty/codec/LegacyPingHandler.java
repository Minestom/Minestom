package net.minestom.server.network.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.ServerListPingType;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class LegacyPingHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf buf;

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object object) {
        final ByteBuf buf = (ByteBuf) object;

        if (this.buf != null) {
            try {
                handle1_6(ctx, buf);
            } finally {
                buf.release();
            }
            return;
        }

        buf.markReaderIndex();

        boolean flag = true;

        try {
            if (buf.readUnsignedByte() == 0xFE) {
                int length = buf.readableBytes();

                switch (length) {
                    case 0:
                        if (trySendResponse(ServerListPingType.LEGACY_UNVERSIONED, ctx)) return;
                        break;
                    case 1:
                        if (buf.readUnsignedByte() != 1) return;

                        if (trySendResponse(ServerListPingType.LEGACY_VERSIONED, ctx)) return;
                        break;
                    default:
                        if (buf.readUnsignedByte() != 0x01 || buf.readUnsignedByte() != 0xFA) return;

                        handle1_6(ctx, buf);
                        break;
                }

                buf.release();
                flag = false;
            }
        } finally {
            if (flag) {
                buf.resetReaderIndex();
                ctx.channel().pipeline().remove("legacy-ping");
                ctx.fireChannelRead(object);
            }
        }
    }

    private void handle1_6(ChannelHandlerContext ctx, ByteBuf part) {
        ByteBuf buf = this.buf;

        if (buf == null) {
            this.buf = buf = ctx.alloc().buffer();
            buf.markReaderIndex();
        } else {
            buf.resetReaderIndex();
        }

        buf.writeBytes(part);

        if (!buf.isReadable(Short.BYTES + Short.BYTES + Byte.BYTES + Short.BYTES + Integer.BYTES)) {
            return;
        }

        final String s = readLegacyString(buf);

        if (s == null) {
            return;
        }

        if (!s.equals("MC|PingHost")) {
            removeHandler(ctx);
            return;
        }

        if (!buf.isReadable(Short.BYTES) || !buf.isReadable(buf.readShort())) {
            return;
        }

        int protocolVersion = buf.readByte();

        if (readLegacyString(buf) == null) {
            removeHandler(ctx);
            return;
        }

        buf.skipBytes(4); // port

        if (buf.isReadable()) {
            removeHandler(ctx);
            return;
        }

        buf.release();

        this.buf = null;

        trySendResponse(ServerListPingType.LEGACY_VERSIONED, ctx);
    }

    private void removeHandler(ChannelHandlerContext ctx) {
        ByteBuf buf = this.buf;
        this.buf = null;

        buf.resetReaderIndex();
        ctx.pipeline().remove(this);
        ctx.fireChannelRead(buf);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        if (this.buf != null) {
            this.buf.release();
            this.buf = null;
        }
    }

    /**
     * Calls a {@link ServerListPingEvent} and sends the response, if the event was not cancelled.
     *
     * @param version the version
     * @param ctx     the context
     * @return {@code true} if the response was cancelled, {@code false} otherwise
     */
    private static boolean trySendResponse(@NotNull ServerListPingType version, @NotNull ChannelHandlerContext ctx) {
        final ServerListPingEvent event = new ServerListPingEvent(version);
        EventDispatcher.call(event);

        if (event.isCancelled()) {
            return true;
        } else {
            // get the response string
            String s = version.getPingResponse(event.getResponseData());

            // create the buffer
            ByteBuf response = Unpooled.buffer();
            response.writeByte(255);

            final char[] chars = s.toCharArray();

            response.writeShort(chars.length);

            for (char c : chars) {
                response.writeChar(c);
            }

            // write the buffer
            ctx.pipeline().firstContext().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

            return false;
        }
    }

    private static String readLegacyString(ByteBuf buf) {
        int size = buf.readShort() * Character.BYTES;
        if (!buf.isReadable(size)) {
            return null;
        }

        final String result = buf.toString(buf.readerIndex(), size, StandardCharsets.UTF_16BE);
        buf.skipBytes(size);

        return result;
    }
}
