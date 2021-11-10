package net.minestom.server.network.socket;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.PacketProcessor;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public final class Server {
    public static final int WORKER_COUNT = Integer.getInteger("minestom.workers",
            Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
    public static final int MAX_PACKET_SIZE = Integer.getInteger("minestom.max-packet-size", 2_097_151); // 3 bytes var-int
    public static final int SOCKET_SEND_BUFFER_SIZE = Integer.getInteger("minestom.send-buffer-size", 262_143);
    public static final int SOCKET_RECEIVE_BUFFER_SIZE = Integer.getInteger("minestom.receive-buffer-size", 32_767);

    public static final boolean NO_DELAY = true;

    private volatile boolean stop;

    private final Selector selector = Selector.open();
    private final List<Worker> workers = new ArrayList<>(WORKER_COUNT);
    private int index;

    private ServerSocketChannel serverSocket;
    private String address;
    private int port;

    public Server(PacketProcessor packetProcessor) throws IOException {
        // Create all workers
        for (int i = 0; i < WORKER_COUNT; i++) {
            Worker worker = new Worker(this, packetProcessor);
            this.workers.add(worker);
            worker.start();
        }
    }

    @ApiStatus.Internal
    public void init(SocketAddress address) throws IOException {
        if (address instanceof InetSocketAddress inetSocketAddress) {
            this.address = inetSocketAddress.getHostString();
            this.port = inetSocketAddress.getPort();
        } // TODO unix domain support

        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(address);
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        this.serverSocket = server;
    }

    @ApiStatus.Internal
    public void start() {
        new Thread(() -> {
            while (!stop) {
                // Busy wait for connections
                try {
                    this.selector.select(key -> {
                        if (!key.isAcceptable()) return;
                        try {
                            // Register socket and forward to thread
                            Worker worker = findWorker();
                            final SocketChannel client = serverSocket.accept();
                            worker.receiveConnection(client);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }
        }, "Ms-entrypoint").start();
    }

    public boolean isOpen() {
        return !stop;
    }

    public void stop() {
        this.stop = true;
        this.selector.wakeup();
        this.workers.forEach(worker -> worker.selector.wakeup());
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    private Worker findWorker() {
        this.index = ++index % WORKER_COUNT;
        return workers.get(index);
    }
}
