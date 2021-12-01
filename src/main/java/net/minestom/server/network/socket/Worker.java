package net.minestom.server.network.socket;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.thread.MinestomThread;
import net.minestom.server.utils.binary.BinaryBuffer;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Inflater;

@ApiStatus.Internal
public final class Worker extends MinestomThread {
    private static final AtomicInteger COUNTER = new AtomicInteger();

    final Selector selector;
    private final Context context = new Context();
    private final Map<SocketChannel, PlayerSocketConnection> connectionMap = new ConcurrentHashMap<>();
    private final Server server;
    private final MpscUnboundedXaddArrayQueue<Runnable> queue = new MpscUnboundedXaddArrayQueue<>(1024);
    private final AtomicBoolean flush = new AtomicBoolean();

    Worker(Server server) {
        super("Ms-worker-" + COUNTER.getAndIncrement());
        this.server = server;
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (server.isOpen()) {
            try {
                try {
                    this.queue.drain(Runnable::run);
                } catch (Exception e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
                // Flush all connections if needed
                if (flush.compareAndSet(true, false)) {
                    try {
                        connectionMap.values().forEach(PlayerSocketConnection::flushSync);
                    } catch (Exception e) {
                        MinecraftServer.getExceptionManager().handleException(e);
                    }
                }
                // Wait for an event
                this.selector.select(key -> {
                    final SocketChannel channel = (SocketChannel) key.channel();
                    if (!channel.isOpen()) return;
                    if (!key.isReadable()) return;
                    PlayerSocketConnection connection = connectionMap.get(channel);
                    try {
                        BinaryBuffer readBuffer = context.readBuffer.clear();
                        // Consume last incomplete packet
                        connection.consumeCache(readBuffer);
                        // Read & process
                        readBuffer.readChannel(channel);
                        connection.processPackets(context, server.packetProcessor());
                    } catch (IOException e) {
                        // TODO print exception? (should ignore disconnection)
                        connection.disconnect();
                    } catch (IllegalArgumentException e) {
                        MinecraftServer.getExceptionManager().handleException(e);
                        connection.disconnect();
                    }
                });
            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        }
    }

    public void disconnect(PlayerSocketConnection connection, SocketChannel channel) {
        try {
            channel.close();
            this.connectionMap.remove(channel);
            MinecraftServer.getConnectionManager().removePlayer(connection);
            connection.refreshOnline(false);
            Player player = connection.getPlayer();
            if (player != null && !player.isRemoved()) {
                player.scheduleNextTick(Entity::remove);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void receiveConnection(SocketChannel channel) throws IOException {
        this.connectionMap.put(channel, new PlayerSocketConnection(this, channel, channel.getRemoteAddress()));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        Socket socket = channel.socket();
        socket.setSendBufferSize(Server.SOCKET_SEND_BUFFER_SIZE);
        socket.setReceiveBufferSize(Server.SOCKET_RECEIVE_BUFFER_SIZE);
        socket.setTcpNoDelay(Server.NO_DELAY);
        socket.setSoTimeout(30 * 1000); // 30 seconds
        this.selector.wakeup();
    }

    public void flush() {
        this.flush.set(true);
        this.selector.wakeup();
    }

    public MessagePassingQueue<Runnable> queue() {
        return queue;
    }

    /**
     * Contains objects that we can be shared across all the connection of a {@link Worker worker}.
     */
    public static final class Context {
        public final BinaryBuffer readBuffer = BinaryBuffer.ofSize(Server.MAX_PACKET_SIZE);
        public final BinaryBuffer contentBuffer = BinaryBuffer.ofSize(Server.MAX_PACKET_SIZE);
        public final Inflater inflater = new Inflater();
    }
}
