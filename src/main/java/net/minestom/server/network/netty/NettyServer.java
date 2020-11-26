package net.minestom.server.network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import io.netty.incubator.channel.uring.IOUring;
import io.netty.incubator.channel.uring.IOUringEventLoopGroup;
import io.netty.incubator.channel.uring.IOUringServerSocketChannel;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.netty.channel.ClientChannel;
import net.minestom.server.network.netty.codec.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class NettyServer {

    public static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    private static final long DEFAULT_COMPRESSED_CHANNEL_WRITE_LIMIT = 600_000L;
    private static final long DEFAULT_COMPRESSED_CHANNEL_READ_LIMIT = 100_000L;

    private static final long DEFAULT_UNCOMPRESSED_CHANNEL_WRITE_LIMIT = 15_000_000L;
    private static final long DEFAULT_UNCOMPRESSED_CHANNEL_READ_LIMIT = 1_000_000L;

    public static final String TRAFFIC_LIMITER_HANDLER_NAME = "traffic-limiter"; // Read/write
    public static final String LEGACY_PING_HANDLER_NAME = "legacy-ping"; // Read

    public static final String ENCRYPT_HANDLER_NAME = "encrypt"; // Write
    public static final String DECRYPT_HANDLER_NAME = "decrypt"; // Read

    public static final String GROUPED_PACKET_HANDLER_NAME = "grouped-packet"; // Write
    public static final String FRAMER_HANDLER_NAME = "framer"; // Read/write

    public static final String COMPRESSOR_HANDLER_NAME = "compressor"; // Read/write

    public static final String DECODER_HANDLER_NAME = "decoder"; // Read
    public static final String ENCODER_HANDLER_NAME = "encoder"; // Write
    public static final String CLIENT_CHANNEL_NAME = "handler"; // Read

    private final EventLoopGroup boss, worker;
    private final ServerBootstrap bootstrap;

    private ServerSocketChannel serverChannel;

    private String address;
    private int port;

    private final GlobalChannelTrafficShapingHandler globalTrafficHandler;

    /**
     * Scheduler used by {@code globalTrafficHandler}.
     */
    private final ScheduledExecutorService trafficScheduler = Executors.newScheduledThreadPool(1);

    public NettyServer(@NotNull PacketProcessor packetProcessor) {
        Class<? extends ServerChannel> channel;

        if (IOUring.isAvailable()) {
            boss = new IOUringEventLoopGroup(2);
            worker = new IOUringEventLoopGroup(); // thread count = core * 2

            channel = IOUringServerSocketChannel.class;

            LOGGER.info("Using io_uring");
        } else if (Epoll.isAvailable()) {
            boss = new EpollEventLoopGroup(2);
            worker = new EpollEventLoopGroup(); // thread count = core * 2

            channel = EpollServerSocketChannel.class;

            LOGGER.info("Using epoll");
        } else if (KQueue.isAvailable()) {
            boss = new KQueueEventLoopGroup(2);
            worker = new KQueueEventLoopGroup(); // thread count = core * 2

            channel = KQueueServerSocketChannel.class;

            LOGGER.info("Using kqueue");
        } else {
            boss = new NioEventLoopGroup(2);
            worker = new NioEventLoopGroup(); // thread count = core * 2

            channel = NioServerSocketChannel.class;

            LOGGER.info("Using NIO");
        }

        bootstrap = new ServerBootstrap()
                .group(boss, worker)
                .channel(channel);

        this.globalTrafficHandler = new GlobalChannelTrafficShapingHandler(trafficScheduler, 200) {
            @Override
            protected void doAccounting(TrafficCounter counter) {
                // TODO proper monitoring API
                //System.out.println("data " + counter.lastWriteThroughput() / 1000 + " " + counter.lastReadThroughput() / 1000);
            }
        };


        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(@NotNull SocketChannel ch) {
                ChannelConfig config = ch.config();
                config.setOption(ChannelOption.TCP_NODELAY, true);
                config.setOption(ChannelOption.SO_SNDBUF, 1_000_000);
                config.setAllocator(ByteBufAllocator.DEFAULT);

                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast(TRAFFIC_LIMITER_HANDLER_NAME, globalTrafficHandler);

                // First check should verify if the packet is a legacy ping (from 1.6 version and earlier)
                // Removed from the pipeline later in LegacyPingHandler if unnecessary (>1.6)
                pipeline.addLast(LEGACY_PING_HANDLER_NAME, new LegacyPingHandler());

                // Used to bypass all the previous handlers by directly sending a framed buffer
                pipeline.addLast(GROUPED_PACKET_HANDLER_NAME, new GroupedPacketHandler());

                // Adds packetLength at start | Reads framed bytebuf
                pipeline.addLast(FRAMER_HANDLER_NAME, new PacketFramer(packetProcessor));

                // Reads bytebuf and creating inbound packet
                pipeline.addLast(DECODER_HANDLER_NAME, new PacketDecoder());

                // Writes packet to bytebuf
                pipeline.addLast(ENCODER_HANDLER_NAME, new PacketEncoder());

                pipeline.addLast(CLIENT_CHANNEL_NAME, new ClientChannel(packetProcessor));
            }
        });
    }

    /**
     * Binds the address to start the server.
     *
     * @param address the server address
     * @param port    the server port
     */
    public void start(@NotNull String address, int port) {

        {
            final boolean compression = MinecraftServer.getCompressionThreshold() != 0;
            if (compression) {
                globalTrafficHandler.setWriteChannelLimit(DEFAULT_COMPRESSED_CHANNEL_WRITE_LIMIT);
                globalTrafficHandler.setReadChannelLimit(DEFAULT_COMPRESSED_CHANNEL_READ_LIMIT);
            } else {
                globalTrafficHandler.setWriteChannelLimit(DEFAULT_UNCOMPRESSED_CHANNEL_WRITE_LIMIT);
                globalTrafficHandler.setReadChannelLimit(DEFAULT_UNCOMPRESSED_CHANNEL_READ_LIMIT);
            }
        }

        this.address = address;
        this.port = port;

        try {
            ChannelFuture cf = bootstrap.bind(new InetSocketAddress(address, port)).sync();

            if (!cf.isSuccess()) {
                throw new IllegalStateException("Unable to bind server at " + address + ":" + port);
            }

            serverChannel = (ServerSocketChannel) cf.channel();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets the address of the server.
     *
     * @return the server address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the port used by the server.
     *
     * @return the server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the traffic handler, used to control channel and global bandwidth.
     * <p>
     * The object can be modified as specified by Netty documentation.
     *
     * @return the global traffic handler
     */
    @NotNull
    public GlobalChannelTrafficShapingHandler getGlobalTrafficHandler() {
        return globalTrafficHandler;
    }

    /**
     * Stops the server and the various services.
     */
    public void stop() {
        try {
            this.serverChannel.close().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            this.worker.shutdownGracefully().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            this.boss.shutdownGracefully().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.trafficScheduler.shutdown();
        this.globalTrafficHandler.release();
    }
}
