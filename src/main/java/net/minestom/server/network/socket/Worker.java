package net.minestom.server.network.socket;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.binary.BinaryBuffer;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.zip.Inflater;

@ApiStatus.Internal
public final class Worker {
    final Selector selector = Selector.open();
    private final Map<SocketChannel, PlayerSocketConnection> connectionMap = new ConcurrentHashMap<>();
    private final PacketProcessor packetProcessor;

    public Worker(Server server, PacketProcessor packetProcessor) throws IOException {
        this.packetProcessor = packetProcessor;
        Thread.start(server, this::threadTick);
    }

    private void threadTick(Context workerContext) {
        try {
            this.selector.select(key -> {
                final SocketChannel channel = (SocketChannel) key.channel();
                if (!channel.isOpen()) return;
                if (!key.isReadable()) return;
                var connection = connectionMap.get(channel);
                try {
                    var readBuffer = workerContext.readBuffer;
                    // Consume last incomplete packet
                    connection.consumeCache(readBuffer);
                    // Read & process
                    readBuffer.readChannel(channel);
                    connection.processPackets(workerContext, packetProcessor);
                } catch (IOException e) {
                    // TODO print exception? (should ignore disconnection)
                    connection.disconnect();
                } finally {
                    workerContext.clearBuffers();
                }
            });
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    public void receiveConnection(SocketChannel channel) throws IOException {
        this.connectionMap.put(channel, new PlayerSocketConnection(this, channel, channel.getRemoteAddress()));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        var socket = channel.socket();
        socket.setSendBufferSize(Server.SOCKET_BUFFER_SIZE);
        socket.setReceiveBufferSize(Server.SOCKET_BUFFER_SIZE);
        socket.setTcpNoDelay(Server.NO_DELAY);
        this.selector.wakeup();
    }

    public void disconnect(PlayerSocketConnection connection, SocketChannel channel) {
        try {
            channel.close();
            this.connectionMap.remove(channel);
            MinecraftServer.getConnectionManager().removePlayer(connection);
            connection.refreshOnline(false);
            Player player = connection.getPlayer();
            if (player != null) {
                player.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Thread responsible for reading players socket and forwarding packets into
     * players' packet queue.
     */
    static final class Thread extends java.lang.Thread {
        private static final AtomicInteger COUNTER = new AtomicInteger();

        private Thread(Runnable runnable) {
            super(null, runnable, "Ms-worker-" + COUNTER.getAndIncrement());
        }

        static void start(Server server, Consumer<Context> runnable) {
            new Thread(() -> {
                Context context = new Context();
                while (server.isOpen()) {
                    try {
                        runnable.accept(context);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * Contains objects that we can be shared across all the connection of a {@link Worker worker}.
     */
    public static final class Context {
        public final BinaryBuffer readBuffer = BinaryBuffer.ofSize(Server.SOCKET_BUFFER_SIZE);
        /**
         * Stores a single packet payload to be read.
         */
        public final BinaryBuffer contentBuffer = BinaryBuffer.ofSize(Server.MAX_PACKET_SIZE);
        public final Inflater inflater = new Inflater();

        public void clearBuffers() {
            this.readBuffer.clear();
            this.contentBuffer.clear();
        }
    }
}
