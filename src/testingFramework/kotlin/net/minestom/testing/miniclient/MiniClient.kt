package net.minestom.testing.miniclient

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import net.minestom.server.MinecraftServer
import net.minestom.server.network.ConnectionState
import net.minestom.server.network.netty.NettyServer
import net.minestom.server.network.netty.codec.*
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.network.packet.client.handshake.HandshakePacket
import net.minestom.server.network.packet.client.login.LoginStartPacket
import net.minestom.server.network.packet.client.status.StatusRequestPacket
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedTransferQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

/**
 * MiniClient that can connect to a Minecraft server. Used for testing Minestom.
 *
 * This is *not* meant to be used as a custom client to play the game.
 */
class MiniClient(val username: String): ChannelInitializer<SocketChannel>() {

    private var port = 25565 /*TODO: random port?*/
    private var packetQueue = LinkedTransferQueue<ClientPacket>()
    private val playerUUID: UUID
    var running = false
    var compressionThreshold = -1
    private val packetProcessor = ClientSidePacketProcessor(this)

    val playerInfo = PlayerInfo("", UUID(0,0))
    val entityId get() = playerInfo.entityID

    val serverConnection = ServerConnection(this)
    var remoteAddress: SocketAddress = InetSocketAddress("localhost", 0)
        private set
    private lateinit var nettyChannel: Channel
    var connectionConfirmed = false
        private set
    private var receivedPackets = ConcurrentHashMap<Class<out ServerPacket>, CopyOnWriteArrayList<ServerPacket>>()

    private var networkLock = ReentrantLock()
    private var networkIdleCondition = networkLock.newCondition()

    init {
        running = true
        playerUUID = UUID.randomUUID()

        setupNetty("localhost", port) {
            remoteAddress = it.remoteAddress()
            login(it, username)

            while(serverConnection.connectionState != ConnectionState.PLAY) {
                Thread.yield()
            }
            while(running) {
                networkLock.withLock {
                    while(packetQueue.isNotEmpty()) {
                        val packet = packetQueue.poll()
                        it.write(packet)
                    }
                    networkIdleCondition.signal()
                    it.flush()
                }
                Thread.yield()
            }
            it.close()
        }

        // wait for login
        while(serverConnection.connectionState != ConnectionState.PLAY) {
            Thread.yield()
        }

        println("CONNECTED 2")
    }

    fun sendPacket(p: ClientPacket) {
        networkLock.withLock {
            packetQueue.add(p)
        }
    }

    fun <Packet : ServerPacket> expect(toExpect: Class<Packet>): List<Packet> {
        return receivedPackets
                .filterKeys { toExpect.isAssignableFrom(it) }
                .map { receivedPackets[it.key] ?: emptyList() }
                .flatten()
                .toList() as List<Packet>
    }

    fun <Packet : ServerPacket> expectSingle(toExpect: Class<Packet>): Packet? {
        return expect(toExpect).firstOrNull()
    }

    /**
     * Performs the handshake to the server
     */
    private fun handshake(channel: Channel, nextState: ConnectionState): ChannelFuture {
        return channel.writeAndFlush(HandshakePacket.setupFromClient(MinecraftServer.PROTOCOL_VERSION, "localhost", port, nextState.ordinal))!!
    }

    /**
     * Login onto the server with the given username
     */
    private fun login(channel: Channel, username: String) {
        handshake(channel, ConnectionState.LOGIN).addListener {
            if(it.isSuccess) {
                serverConnection.connectionState = ConnectionState.LOGIN
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
        handshake(channel, ConnectionState.STATUS).addListener {
            if(it.isSuccess) {
                serverConnection.connectionState = ConnectionState.STATUS
                channel.writeAndFlush(StatusRequestPacket())
            }
        }
    }

    fun setupNetty(serverAddress: String, port: Int, block: (Channel) -> Unit) {
        thread(name = "Netty Thread - $username") {
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

    override fun initChannel(ch: SocketChannel) {
        this.nettyChannel = ch
        ch.let {
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
        if(this::nettyChannel.isInitialized) {
            nettyChannel.closeFuture().sync()
        }
    }

    fun startCompression() {
        nettyChannel.pipeline().addAfter(NettyServer.FRAMER_HANDLER_NAME, NettyServer.COMPRESSOR_HANDLER_NAME,
                PacketCompressor(compressionThreshold))
    }

    fun confirmConnection() {
        connectionConfirmed = true
    }

    fun waitNetworkIdle() {
        networkLock.withLock {
            networkIdleCondition.await()
        }
    }

    fun receivePacket(playPacket: ServerPacket) {
        val list = receivedPackets.computeIfAbsent(playPacket.javaClass) { CopyOnWriteArrayList() }
        list.add(playPacket)
    }

    fun <Packet: ServerPacket> waitForPacket(clazz: Class<Packet>): Packet {
        var packet = expectSingle(clazz)
        while(packet == null) {
            Thread.yield()
            packet = expectSingle(clazz)
        }
        (receivedPackets[packet.javaClass] ?: error("No packets of type ${packet.javaClass.canonicalName}")).remove(packet)
        return packet
    }
}