package net.minestom.server.extras.query;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.extras.query.event.BasicQueryEvent;
import net.minestom.server.extras.query.event.FullQueryEvent;
import net.minestom.server.extras.query.response.BasicQueryResponse;
import net.minestom.server.extras.query.response.FullQueryResponse;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * GameSpy4 Query Protocol implementation backed by Netty UDP rather than
 * {@code java.net.DatagramSocket}.
 *
 * <p>No {@code java.nio.channels.*} or {@code sun.misc.Unsafe} references;
 * raw byte I/O is performed through Netty's {@link ByteBuf}.
 *
 * @see <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Query">
 *     Minecraft wiki – Query protocol</a>
 */
public class Query {

    public static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    private static final Logger LOGGER   = LoggerFactory.getLogger(Query.class);
    private static final Random RANDOM   = new Random();

    /** challenge-token -> sender address */
    private static final Int2ObjectMap<SocketAddress> CHALLENGE_TOKENS =
            Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    private static volatile boolean       started;
    private static volatile Channel       udpChannel;
    private static volatile EventLoopGroup eventLoopGroup;
    private static volatile Task          task;

    private Query() {}

    /**
     * Starts the query system on an OS-assigned port.
     *
     * @return the bound port
     * @throws IllegalArgumentException if already running
     */
    public static int start() {
        if (udpChannel != null) throw new IllegalArgumentException("System is already running");
        start(0);
        return ((InetSocketAddress) udpChannel.localAddress()).getPort();
    }

    /**
     * Starts the query system on the given {@code port}.
     *
     * @return {@code true} on success, {@code false} if already running or
     *         if the port could not be bound
     */
    public static boolean start(int port) {
        if (udpChannel != null) return false;

        final EventLoopGroup group = new NioEventLoopGroup(1);
        final Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioDatagramChannel.class)
                .handler(new QueryHandler());

        try {
            udpChannel      = bootstrap.bind(port).sync().channel();
            eventLoopGroup  = group;
            started         = true;
        } catch (Exception e) {
            LOGGER.warn("Could not open the query port!", e);
            group.shutdownGracefully();
            return false;
        }

        task = MinecraftServer.getSchedulerManager()
                .buildTask(CHALLENGE_TOKENS::clear)
                .repeat(30, TimeUnit.SECOND)
                .schedule();

        return true;
    }

    /**
     * Stops the query system.
     *
     * @return {@code true} if it was running, {@code false} otherwise
     */
    public static boolean stop() {
        if (!started) return false;

        started = false;
        if (udpChannel != null) {
            udpChannel.close().awaitUninterruptibly(); udpChannel = null;
        }

        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();        eventLoopGroup = null;
        }

        if (task != null) {
            task.cancel(); task = null;
        }

        CHALLENGE_TOKENS.clear();
        return true;
    }

    /** @return {@code true} if the query system is currently running */
    public static boolean isStarted() {
        return started;
    }

    /**
     * Handles inbound UDP datagrams and replies inline on the same Netty
     * event-loop thread — no extra thread needed.
     */
    @ChannelHandler.Sharable
    private static final class QueryHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
            final ByteBuf data   = msg.content();
            final InetSocketAddress sender = msg.sender();

            // Check magic 0xFEFD
            if (data.readableBytes() < 3) return;
            final int magic = data.readUnsignedShort();
            if (magic != 0xFEFD) return;

            final byte type = data.readByte();

            if (type == 9) { // handshake
                if (data.readableBytes() < 4) return;
                final int sessionID      = data.readInt();
                final int challengeToken = RANDOM.nextInt();
                CHALLENGE_TOKENS.put(challengeToken, sender);

                final byte[] responseBytes = NetworkBuffer.makeArray(buf -> {
                    buf.write(NetworkBuffer.BYTE, (byte) 9);
                    buf.write(NetworkBuffer.INT, sessionID);
                    buf.write(NetworkBuffer.STRING_TERMINATED, String.valueOf(challengeToken));
                });

                send(ctx, sender, responseBytes);

            } else if (type == 0) { // stat
                if (data.readableBytes() < 8) return;
                final int sessionID      = data.readInt();
                final int challengeToken = data.readInt();
                final int remaining      = data.readableBytes();

                if (!CHALLENGE_TOKENS.containsKey(challengeToken)
                        || !CHALLENGE_TOKENS.get(challengeToken).equals(sender)) return;

                if (remaining == 0) { // basic query
                    final BasicQueryEvent event = new BasicQueryEvent(sender, sessionID);
                    EventDispatcher.callCancellable(event, () ->
                            sendQueryResponse(ctx, BasicQueryResponse.SERIALIZER,
                                    event.getQueryResponse(), sessionID, sender));
                } else if (remaining == 5) { // full query
                    final FullQueryEvent event = new FullQueryEvent(sender, sessionID);
                    EventDispatcher.callCancellable(event, () ->
                            sendQueryResponse(ctx, FullQueryResponse.SERIALIZER,
                                    event.getQueryResponse(), sessionID, sender));
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (started) {
                LOGGER.error("Error in query handler", cause);
            }
        }
    }

    private static <T> void sendQueryResponse(ChannelHandlerContext ctx,
                                              NetworkBuffer.Type<T> type, T response,
                                              int sessionID, InetSocketAddress sender) {
        final byte[] payload = NetworkBuffer.makeArray(buf -> {
            buf.write(NetworkBuffer.BYTE, (byte) 0);
            buf.write(NetworkBuffer.INT, sessionID);
            buf.write(type, response);
        });
        send(ctx, sender, payload);
    }

    private static void send(ChannelHandlerContext ctx,
                             InetSocketAddress recipient, byte[] data) {
        final ByteBuf buf = ctx.alloc().buffer(data.length).writeBytes(data);
        ctx.writeAndFlush(new DatagramPacket(buf, recipient))
                .addListener(f -> {
                    if (!f.isSuccess() && started) {
                        LOGGER.error("Failed to send query response to {}", recipient, f.cause());
                    }
                });
    }
}