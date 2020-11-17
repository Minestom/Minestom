package net.minestom.server.network.netty;

import io.netty.bootstrap.ServerBootstrap;
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
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.netty.channel.ClientChannel;
import net.minestom.server.network.netty.codec.LegacyPingHandler;
import net.minestom.server.network.netty.codec.PacketDecoder;
import net.minestom.server.network.netty.codec.PacketEncoder;
import net.minestom.server.network.netty.codec.PacketFramer;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

public class NettyServer {

    private final EventLoopGroup boss, worker;
    private final ServerBootstrap bootstrap;

    private ServerSocketChannel serverChannel;

    private String address;
    private int port;

    // Options
    private long writeLimit;
    private long readLimit;

    public NettyServer(@NotNull PacketProcessor packetProcessor) {
        Class<? extends ServerChannel> channel;

        if (Epoll.isAvailable()) {
            boss = new EpollEventLoopGroup(2);
            worker = new EpollEventLoopGroup(); // thread count = core * 2

            channel = EpollServerSocketChannel.class;
        } else if (KQueue.isAvailable()) {
            boss = new KQueueEventLoopGroup(2);
            worker = new KQueueEventLoopGroup(); // thread count = core * 2

            channel = KQueueServerSocketChannel.class;
        } else {
            boss = new NioEventLoopGroup(2);
            worker = new NioEventLoopGroup(); // thread count = core * 2

            channel = NioServerSocketChannel.class;
        }

        bootstrap = new ServerBootstrap()
                .group(boss, worker)
                .channel(channel);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(@NotNull SocketChannel ch) {
                ChannelConfig config = ch.config();
                config.setOption(ChannelOption.TCP_NODELAY, true);

                ChannelPipeline pipeline = ch.pipeline();

                ChannelTrafficShapingHandler channelTrafficShapingHandler =
                        new ChannelTrafficShapingHandler(writeLimit, readLimit, 200);

                pipeline.addLast("traffic-limiter", channelTrafficShapingHandler);

                // First check should verify if the packet is a legacy ping (from 1.6 version and earlier)
                pipeline.addLast("legacy-ping", new LegacyPingHandler());

                // Adds packetLength at start | Reads framed bytebuf
                pipeline.addLast("framer", new PacketFramer(packetProcessor));

                // Reads bytebuf and creating inbound packet
                pipeline.addLast("decoder", new PacketDecoder());

                // Writes packet to bytebuf
                pipeline.addLast("encoder", new PacketEncoder());

                pipeline.addLast("handler", new ClientChannel(packetProcessor));
            }
        });
    }

    public void start(String address, int port) {
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
     * Gets the server write limit.
     * <p>
     * Used when you want to limit the bandwidth used by a single connection.
     * Can also prevent the networking threads from being unresponsive.
     *
     * @return the write limit in bytes
     */
    public long getWriteLimit() {
        return writeLimit;
    }

    /**
     * Changes the server write limit
     * <p>
     * WARNING: the change will only apply to new connections, the current ones will not be updated.
     *
     * @param writeLimit the new write limit in bytes, 0 to disable
     * @see #getWriteLimit()
     */
    public void setWriteLimit(long writeLimit) {
        this.writeLimit = writeLimit;
    }


    /**
     * Gets the server read limit.
     * <p>
     * Used when you want to limit the bandwidth used by a single connection.
     * Can also prevent the networking threads from being unresponsive.
     *
     * @return the read limit in bytes
     */
    public long getReadLimit() {
        return readLimit;
    }

    /**
     * Changes the server read limit
     * <p>
     * WARNING: the change will only apply to new connections, the current ones will not be updated.
     *
     * @param readLimit the new read limit in bytes, 0 to disable
     * @see #getWriteLimit()
     */
    public void setReadLimit(long readLimit) {
        this.readLimit = readLimit;
    }

    public void stop() {
        serverChannel.close();

        worker.shutdownGracefully();
        boss.shutdownGracefully();
    }
}
