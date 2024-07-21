package net.minestom.server.network.socket;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.packet.PacketParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private volatile boolean stop;

    private final Selector selector = Selector.open();
    private final PacketParser.Client packetParser;
    private final List<Worker> workers;
    private int index;

    private ServerSocketChannel serverSocket;
    private SocketAddress socketAddress;
    private String address;
    private int port;

    public Server(PacketParser.Client packetParser) throws IOException {
        this.packetParser = packetParser;
        Worker[] workers = new Worker[ServerFlag.WORKER_COUNT];
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
            if (serverSocket != null) {
                this.serverSocket.close();
            }

            if (socketAddress instanceof UnixDomainSocketAddress unixDomainSocketAddress) {
                Files.deleteIfExists(unixDomainSocketAddress.getPath());
            }
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
        try {
            this.selector.wakeup();
            this.selector.close();
        } catch (IOException e) {
            LOGGER.error("Server socket selector could not be closed", e);
            System.exit(-1);
        }
        this.workers.forEach(Worker::close);
    }

    @ApiStatus.Internal
    public @NotNull PacketParser.Client packetParser() {
        return packetParser;
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
        this.index = ++index % ServerFlag.WORKER_COUNT;
        return workers.get(index);
    }
}
