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
import io.netty.incubator.channel.uring.IOUring;
import io.netty.incubator.channel.uring.IOUringEventLoopGroup;
import io.netty.incubator.channel.uring.IOUringServerSocketChannel;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.netty.channel.ClientChannel;
import net.minestom.server.network.netty.codec.*;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public final class NettyServer {

    public static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);
    public static final int BUFFER_SIZE = Integer.getInteger("minestom.channel-buffer-size", 65535);

    private static final WriteBufferWaterMark SERVER_WRITE_MARK = new WriteBufferWaterMark(1 << 20,
            1 << 21);

    public static final String LEGACY_PING_HANDLER_NAME = "legacy-ping"; // Read

    public static final String ENCRYPT_HANDLER_NAME = "encrypt"; // Write
    public static final String DECRYPT_HANDLER_NAME = "decrypt"; // Read

    public static final String GROUPED_PACKET_HANDLER_NAME = "grouped-packet"; // Write
    public static final String FRAMER_HANDLER_NAME = "framer"; // Read/write

    public static final String COMPRESSOR_HANDLER_NAME = "compressor"; // Read/write

    public static final String DECODER_HANDLER_NAME = "decoder"; // Read
    public static final String ENCODER_HANDLER_NAME = "encoder"; // Write
    public static final String CLIENT_CHANNEL_NAME = "handler"; // Read

    private boolean initialized = false;

    private final PacketProcessor packetProcessor;

    private EventLoopGroup boss, worker;
    private ServerBootstrap bootstrap;

    private ServerSocketChannel serverChannel;

    private String address;
    private int port;

    public NettyServer(@NotNull PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
    }

    /**
     * Inits the server by choosing which transport layer to use, number of threads, pipeline order, etc...
     * <p>
     * Called just before {@link #start(String, int)}.
     */
    public void init() {
        Check.stateCondition(initialized, "Netty server has already been initialized!");
        this.initialized = true;

        Class<? extends ServerChannel> channel;
        final int workerThreadCount = MinecraftServer.getNettyThreadCount();

        // Find boss/worker event group
        {
            if (IOUring.isAvailable()) {
                boss = new IOUringEventLoopGroup(2);
                worker = new IOUringEventLoopGroup(workerThreadCount);

                channel = IOUringServerSocketChannel.class;

                LOGGER.info("Using io_uring");
            } else if (Epoll.isAvailable()) {
                boss = new EpollEventLoopGroup(2);
                worker = new EpollEventLoopGroup(workerThreadCount);

                channel = EpollServerSocketChannel.class;

                LOGGER.info("Using epoll");
            } else if (KQueue.isAvailable()) {
                boss = new KQueueEventLoopGroup(2);
                worker = new KQueueEventLoopGroup(workerThreadCount);

                channel = KQueueServerSocketChannel.class;

                LOGGER.info("Using kqueue");
            } else {
                boss = new NioEventLoopGroup(2);
                worker = new NioEventLoopGroup(workerThreadCount);

                channel = NioServerSocketChannel.class;

                LOGGER.info("Using NIO");
            }
        }

        bootstrap = new ServerBootstrap()
                .group(boss, worker)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, SERVER_WRITE_MARK)
                .channel(channel);


        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(@NotNull SocketChannel ch) {
                ChannelConfig config = ch.config();
                config.setOption(ChannelOption.TCP_NODELAY, true);
                config.setOption(ChannelOption.SO_KEEPALIVE, true);
                config.setOption(ChannelOption.SO_SNDBUF, BUFFER_SIZE);
                config.setAllocator(ByteBufAllocator.DEFAULT);

                ChannelPipeline pipeline = ch.pipeline();

                // First check should verify if the packet is a legacy ping (from 1.6 version and earlier)
                // Removed from the pipeline later in LegacyPingHandler if unnecessary (>1.6)
                pipeline.addLast(LEGACY_PING_HANDLER_NAME, new LegacyPingHandler());

                // Used to bypass all the previous handlers by directly sending a framed buffer
                pipeline.addLast(GROUPED_PACKET_HANDLER_NAME, new GroupedPacketHandler());

                // Adds packetLength at start | Reads framed buffer
                pipeline.addLast(FRAMER_HANDLER_NAME, new PacketFramer(packetProcessor));

                // Reads buffer and create inbound packet
                pipeline.addLast(DECODER_HANDLER_NAME, new PacketDecoder());

                // Writes packet to buffer
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
        this.address = address;
        this.port = port;

        // Bind address
        try {
            ChannelFuture cf = bootstrap.bind(new InetSocketAddress(address, port)).sync();

            if (!cf.isSuccess()) {
                throw new IllegalStateException("Unable to bind server at " + address + ":" + port);
            }

            this.serverChannel = (ServerSocketChannel) cf.channel();
        } catch (InterruptedException ex) {
            MinecraftServer.getExceptionManager().handleException(ex);
        }
    }

    /**
     * Gets the address of the server.
     *
     * @return the server address, null if the address isn't bound yet
     */
    @Nullable
    public String getAddress() {
        return address;
    }

    /**
     * Gets the port used by the server.
     *
     * @return the server port, 0 if the address isn't bound yet
     */
    public int getPort() {
        return port;
    }

    /**
     * Stops the server.
     */
    public void stop() {
        try {
            this.boss.shutdownGracefully().sync();
            this.worker.shutdownGracefully().sync();
            this.serverChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
