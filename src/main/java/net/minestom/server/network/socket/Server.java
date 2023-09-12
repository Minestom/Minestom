package net.minestom.server.network.socket;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.PacketProcessor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public final class Server {
    public static final int WORKER_COUNT = Integer.getInteger("minestom.workers", Runtime.getRuntime().availableProcessors());
    public static final int MAX_PACKET_SIZE = Integer.getInteger("minestom.max-packet-size", 2_097_151); // 3 bytes var-int
    public static final int SOCKET_SEND_BUFFER_SIZE = Integer.getInteger("minestom.send-buffer-size", 262_143);
    public static final int SOCKET_RECEIVE_BUFFER_SIZE = Integer.getInteger("minestom.receive-buffer-size", 32_767);

    public static final boolean NO_DELAY = true;

    private volatile boolean stop;

    private final Selector selector = Selector.open();
    private final PacketProcessor packetProcessor;
    private final List<Worker> workers;
    private int index;

    private ServerSocketChannel serverSocket;
    private SocketAddress socketAddress;
    private String address;
    private int port;

    public Server(PacketProcessor packetProcessor) throws IOException {
        this.packetProcessor = packetProcessor;
        Worker[] workers = new Worker[WORKER_COUNT];
        Arrays.setAll(workers, value -> new Worker(this));
        this.workers = List.of(workers);
    }

    @ApiStatus.Internal
    public void init(SocketAddress address) throws IOException {
        ProtocolFamily family;
        if (address instanceof InetSocketAddress inetSocketAddress) {
            this.address = inetSocketAddress.getHostString();
            this.port = inetSocketAddress.getPort();
            family = inetSocketAddress.getAddress().getAddress().length == 4 ? StandardProtocolFamily.INET : StandardProtocolFamily.INET6;
        } else if (address instanceof UnixDomainSocketAddress unixDomainSocketAddress) {
            this.address = "unix://" + unixDomainSocketAddress.getPath();
            this.port = 0;
            family = StandardProtocolFamily.UNIX;
        } else {
            throw new IllegalArgumentException("Address must be an InetSocketAddress or a UnixDomainSocketAddress");
        }

        ServerSocketChannel server = ServerSocketChannel.open(family);
        server.bind(address);
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        this.serverSocket = server;
        this.socketAddress = address;

        if (address instanceof InetSocketAddress && port == 0) {
            port = server.socket().getLocalPort();
        }
    }

    @ApiStatus.Internal
    public void start() {
        this.workers.forEach(Thread::start);
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

    public void tick() {
        this.workers.forEach(Worker::tick);
    }

    public boolean isOpen() {
        return !stop;
    }

    public void stop() {
        this.stop = true;
        try {
            if(serverSocket != null) {
                this.serverSocket.close();
            }

            if (socketAddress instanceof UnixDomainSocketAddress unixDomainSocketAddress) {
                Files.deleteIfExists(unixDomainSocketAddress.getPath());
            }
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
        this.selector.wakeup();
        this.workers.forEach(worker -> worker.selector.wakeup());
    }

    @ApiStatus.Internal
    public @NotNull PacketProcessor packetProcessor() {
        return packetProcessor;
    }

    public SocketAddress socketAddress() {
        return socketAddress;
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
