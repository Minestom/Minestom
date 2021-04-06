package net.minestom.testing.miniclient

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import net.minestom.server.MinecraftServer
import net.minestom.server.network.netty.NettyServer
import net.minestom.server.network.netty.channel.ClientChannel
import net.minestom.server.network.netty.codec.GroupedPacketHandler
import net.minestom.server.network.netty.codec.PacketDecoder
import net.minestom.server.network.netty.codec.PacketEncoder
import net.minestom.server.network.netty.codec.PacketFramer
import java.util.UUID
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.network.packet.client.handshake.HandshakePacket
import net.minestom.server.network.packet.client.login.LoginStartPacket
import net.minestom.server.network.packet.client.status.StatusRequestPacket
import net.minestom.server.network.packet.server.ServerPacket
import java.util.concurrent.LinkedTransferQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * MiniClient that can connect to a Minecraft server. Used for testing Minestom.
 *
 * This is *not* meant to be used as a custom client to play the game.
 */
class MiniClient(val testName: String): ChannelInitializer<SocketChannel>() {

    private enum class NetworkState {
        Play,
        Status,
        Login,
        Handshake
    }
    private data class NetworkSettings(var state: NetworkState, var compressionThreshold: Int = 256)

    private var port = 25565 /*TODO: random port?*/
    private var packetQueue = LinkedTransferQueue<ClientPacket>()
    private val playerUUID: UUID
    var entityId = -1
    var running = false
    private var networkSettings = NetworkSettings(NetworkState.Handshake)
    private val packetProcessor = ClientSidePacketProcessor()
    private var username = ""

    companion object {
        private var counter = AtomicInteger()
    }

    init {
        running = true
        playerUUID = UUID.randomUUID()
        username = "$testName-${counter.getAndIncrement()}"

        setupNetty("localhost", port) {
            login(it, username)

            while(networkSettings.state != NetworkState.Play) {
                Thread.yield()
            }
            while(running) {
                synchronized(packetQueue) {
                    while(packetQueue.isNotEmpty()) {
                        val packet = packetQueue.poll()
                        it.write(packet)
                    }
                    it.flush()
                }
                Thread.yield()
            }
        }

        // wait for login
        while(networkSettings.state != NetworkState.Play) {
            Thread.yield()
        }
    }

    fun sendPacket(p: ClientPacket) {
        synchronized(packetQueue) {
            packetQueue.add(p)
        }
    }

    fun <Packet : ServerPacket?> expect(toExpect: Class<Packet>?): List<Packet>? {
        TODO()
    }

    fun <Packet : ServerPacket?> expectSingle(toExpect: Class<Packet>?): Packet? {
        TODO()
    }

    /**
     * Performs the handshake to the server
     */
    private fun handshake(channel: Channel, nextState: NetworkState): ChannelFuture {
        return channel.writeAndFlush(HandshakePacket.setupFromClient(MinecraftServer.PROTOCOL_VERSION, "localhost", port, nextState.ordinal))!!
    }

    /**
     * Login onto the server with the given username
     */
    private fun login(channel: Channel, username: String) {
        handshake(channel, NetworkState.Login).addListener {
            if(it.isSuccess) {
                networkSettings.state = NetworkState.Login
                channel.writeAndFlush(LoginStartPacket.setupFromClient(username))
            } else if(it.isDone) {
                it.cause().printStackTrace()
            }
        }
    }

    /**
     * Request the status of the server
     */
    private fun requestStatus(channel: Channel) {
        handshake(channel, NetworkState.Status).addListener {
            if(it.isSuccess) {
                networkSettings.state = NetworkState.Status
                channel.writeAndFlush(StatusRequestPacket())
            }
        }
    }

    fun setupNetty(serverAddress: String, port: Int, block: (Channel) -> Unit) {
        thread(name = "Netty Thread") {
            val workGroup = NioEventLoopGroup()
            try {
                val bootstrap = Bootstrap()
                        .group(workGroup)
                        .channel(NioSocketChannel::class.java)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(this)
                val future = bootstrap.connect(serverAddress, port).sync()

                println("Connected!")

                block(future.channel())

                future.channel().closeFuture().sync()
            } finally {
                workGroup.shutdownGracefully()
            }
        }
    }

    override fun initChannel(ch: SocketChannel?) {
        ch?.let {
            // Used to bypass all the previous handlers by directly sending a framed buffer
            ch.pipeline().addLast(NettyServer.GROUPED_PACKET_HANDLER_NAME, GroupedPacketHandler())

            // Adds packetLength at start | Reads framed buffer
            ch.pipeline().addLast(NettyServer.FRAMER_HANDLER_NAME, PacketFramer(packetProcessor))

            // Reads buffer and create inbound packet
            ch.pipeline().addLast(NettyServer.DECODER_HANDLER_NAME, PacketDecoder(username))

            // Writes packet to buffer
            ch.pipeline().addLast(NettyServer.ENCODER_HANDLER_NAME, PacketEncoder())

            ch.pipeline().addLast(NettyServer.CLIENT_CHANNEL_NAME, MiniClientChannel(packetProcessor))

            ch.pipeline().addLast(object: ChannelHandlerAdapter() {
                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                    this@MiniClient.exceptionCaught(ctx, cause)
                }
            })
            ch.pipeline().addFirst(object: ChannelHandlerAdapter() {
                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                    this@MiniClient.exceptionCaught(ctx, cause)
                }
            })
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        super.channelRead(ctx, msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    fun stop() {
        running = false
    }
}