package net.minestom.server.network.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minestom.server.MinecraftServer;

import java.nio.charset.StandardCharsets;

// Copied from original minecraft :(
public class LegacyPingHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf buf;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        ByteBuf buf = (ByteBuf) object;

        if (this.buf != null) {
            try {
                readLegacy1_6(ctx, buf);
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
                        this.writeResponse(ctx, this.createResponse(formatResponse(-2)));
                        break;
                    case 1:
                        if (buf.readUnsignedByte() != 1) {
                            return;
                        }

                        this.writeResponse(ctx, this.createResponse(formatResponse(-1)));
                        break;
                    default:
                        if (buf.readUnsignedByte() != 0x01 || buf.readUnsignedByte() != 0xFA) return;

                        readLegacy1_6(ctx, buf);
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

    private static String readLegacyString(ByteBuf buf) {
        int size = buf.readShort() * Character.BYTES;
        if (!buf.isReadable(size)) {
            return null;
        }

        String result = buf.toString(buf.readerIndex(), size, StandardCharsets.UTF_16BE);
        buf.skipBytes(size);

        return result;
    }

    private void readLegacy1_6(ChannelHandlerContext ctx, ByteBuf part) {
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

        String s = readLegacyString(buf);

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

        this.writeResponse(ctx, this.createResponse(formatResponse(protocolVersion)));
    }

    private String formatResponse(int playerProtocol) {
        // todo server motd, online and slots
        final String motd = "Minestom";
        final String version = MinecraftServer.VERSION_NAME;
        final int online = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
        final int max = 0;
        final int protocol = MinecraftServer.PROTOCOL_VERSION;

        if (playerProtocol == -2) {
            return String.format(
                    "%s\u00a7%d\u00a7%d",
                    motd, online, max
            );
        }

        return String.format(
                "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d",
                protocol, version, motd, online, max
        );
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

    private void writeResponse(ChannelHandlerContext ctx, ByteBuf buf) {
        ctx.pipeline().firstContext().writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE);
    }

    private ByteBuf createResponse(String s) {
        ByteBuf response = Unpooled.buffer();
        response.writeByte(255);

        char[] chars = s.toCharArray();

        response.writeShort(chars.length);

        for (char c : chars) {
            response.writeChar(c);
        }

        return response;
    }
}
