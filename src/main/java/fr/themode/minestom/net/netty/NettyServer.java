package fr.themode.minestom.net.netty;

import fr.themode.minestom.net.PacketProcessor;
import fr.themode.minestom.net.netty.channel.ClientChannel;
import fr.themode.minestom.net.netty.channel.NettyDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class NettyServer {

    private PacketProcessor packetProcessor;
    private EventLoopGroup group;

    private String address;
    private int port;

    public NettyServer(PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
        this.group = new NioEventLoopGroup();
    }

    public void start(String address, int port) {
        this.address = address;
        this.port = port;

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress(address, port));

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.pipeline().addLast(new NettyDecoder());
                    socketChannel.pipeline().addLast(new ClientChannel(packetProcessor));
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
