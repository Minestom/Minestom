package net.minestom.server.network.socket;

import net.minestom.server.network.PacketProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class Server {
    public static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    public static final int WORKER_COUNT = Integer.getInteger("minestom.workers",
            Runtime.getRuntime().availableProcessors() * 2);
    public static final int SOCKET_BUFFER_SIZE = Integer.getInteger("minestom.buffer-size", 262_143);
    public static final int MAX_PACKET_SIZE = 2_097_151; // 3 bytes var-int
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

    public void start(SocketAddress address) throws IOException {
        this.serverSocket = ServerSocketChannel.open();
        serverSocket.bind(address);
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        serverSocket.socket().setReceiveBufferSize(SOCKET_BUFFER_SIZE);
        LOGGER.info("Server starting, wait for connections");
        new Thread(() -> {
            while (!stop) {
                // Busy wait for connections
                try {
                    serverTick(selector, serverSocket);
                } catch (IOException e) {
                    e.printStackTrace();
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

    private void serverTick(Selector selector, ServerSocketChannel socketChannel) throws IOException {
        selector.select();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        for (SelectionKey key : selectedKeys) {
            if (key.isAcceptable()) {
                // Register socket and forward to thread
                Worker thread = findWorker();
                final SocketChannel client = socketChannel.accept();
                thread.receiveConnection(client);
            }
        }
        selectedKeys.clear();
    }

    private Worker findWorker() {
        this.index = ++index % WORKER_COUNT;
        return workers.get(index);
    }
}
