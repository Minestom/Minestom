package net.minestom.server.network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.netty.channel.ClientChannel;
import net.minestom.server.network.netty.channel.NettyDecoder;

import java.net.InetSocketAddress;

public class NettyServer {

    private final PacketProcessor packetProcessor;

    private final EventLoopGroup boss, worker;
    private final ServerBootstrap bootstrap;

    private ServerSocketChannel serverChannel;

    private String address;
    private int port;

    public NettyServer(PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;

        Class<? extends ServerChannel> channel;

        if (Epoll.isAvailable()) {
            boss = new EpollEventLoopGroup(2);
            worker = new EpollEventLoopGroup();

            channel = EpollServerSocketChannel.class;
        } else {
            boss = new NioEventLoopGroup(2);
            worker = new EpollEventLoopGroup();

            channel = NioServerSocketChannel.class;
        }

        bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker);
        bootstrap.channel(channel);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(new NettyDecoder());
                socketChannel.pipeline().addLast(new ClientChannel(packetProcessor));
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

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public void stop() {
        serverChannel.close();

        worker.shutdownGracefully();
        boss.shutdownGracefully();
    }
}
