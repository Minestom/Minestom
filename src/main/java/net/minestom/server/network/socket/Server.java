package net.minestom.server.network.socket;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.player.PlayerSocketConnection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class Server {
    private volatile boolean stop;

    private final PacketParser.Client packetParser;

    private @UnknownNullability ServerSocketChannel serverSocket;
    private @UnknownNullability SocketAddress socketAddress;
    private @UnknownNullability String address;
    private int port;

    public Server(PacketParser.Client packetParser) {
        this.packetParser = packetParser;
    }

    public Server() {
        this(PacketVanilla.CLIENT_PACKET_PARSER);
    }

    @ApiStatus.Internal
    public void init(SocketAddress address) throws IOException {
        ProtocolFamily family;
        switch (address) {
            case InetSocketAddress inetSocketAddress -> {
                this.address = inetSocketAddress.getHostString();
                this.port = inetSocketAddress.getPort();
                family = inetSocketAddress.getAddress().getAddress().length == 4 ? StandardProtocolFamily.INET : StandardProtocolFamily.INET6;
            }
            case UnixDomainSocketAddress unixDomainSocketAddress -> {
                this.address = "unix://" + unixDomainSocketAddress.getPath();
                this.port = 0;
                family = StandardProtocolFamily.UNIX;
            }
            default ->
                    throw new IllegalArgumentException("Address must be an InetSocketAddress or a UnixDomainSocketAddress");
        }

        ServerSocketChannel server = ServerSocketChannel.open(family);
        server.bind(address);
        this.serverSocket = server;
        this.socketAddress = address;

        if (address instanceof InetSocketAddress && port == 0) {
            port = server.socket().getLocalPort();
        }
    }

    @ApiStatus.Internal
    public void start() {
        Thread.ofVirtual().name("Ms-Socket-Server").start(() -> {
            // Use named thread builders for logging
            var readBuilder = Thread.ofVirtual().name("Ms-Socket-Reader-", 0);
            var writeBuilder = Thread.ofVirtual().name("Ms-Socket-Writer-", 0);
            final var serverSocket = Objects.requireNonNull(this.serverSocket, "Not bound did you forget to call #init?");
            while (!stop) {
                final SocketChannel client;
                try {
                    client = serverSocket.accept();
                } catch (ClosedChannelException e) {
                    break; // We are exiting, bye bye!
                } catch (IOException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                    continue;
                }

                AtomicReference<@UnknownNullability PlayerSocketConnection> reference = new AtomicReference<>(null);
                try {
                    configureSocket(client);
                    Thread readThread = readBuilder.unstarted(() -> playerReadLoop(reference.get()));
                    Thread writeThread = writeBuilder.unstarted(() -> playerWriteLoop(reference.get()));
                    PlayerSocketConnection connection = new PlayerSocketConnection(client, client.getRemoteAddress(), readThread, writeThread);
                    reference.set(connection);
                    readThread.start();
                    writeThread.start();
                } catch (IOException _) {
                    try {
                        client.close();
                    } catch (IOException _) {}
                }
            }
        });
    }

    private void configureSocket(SocketChannel channel) throws IOException {
        if (channel.getLocalAddress() instanceof InetSocketAddress) {
            Socket socket = channel.socket();
            socket.setSendBufferSize(ServerFlag.SOCKET_SEND_BUFFER_SIZE);
            socket.setReceiveBufferSize(ServerFlag.SOCKET_RECEIVE_BUFFER_SIZE);
            socket.setTcpNoDelay(ServerFlag.SOCKET_NO_DELAY);
            socket.setSoTimeout(ServerFlag.SOCKET_TIMEOUT);
        }
    }

    private void playerReadLoop(PlayerSocketConnection connection) {
        Objects.requireNonNull(connection, "connection cannot be null");
        while (!stop) {
            try {
                // Read & process packets
                connection.read(packetParser);
            } catch (ClosedChannelException | EOFException _) {
                connection.disconnect(); // We closed the socket during read, just exit.
                break;
            } catch (Throwable e) {
                boolean isExpected = e instanceof SocketException && e.getMessage().equals("Connection reset");
                if (!isExpected) MinecraftServer.getExceptionManager().handleException(e);
                connection.disconnect();
                break;
            }
        }
    }

    private void playerWriteLoop(PlayerSocketConnection connection) {
        Objects.requireNonNull(connection, "connection cannot be null");
        try {
            while (!stop) {
                try {
                    connection.flushSync();
                } catch (ClosedChannelException | EOFException _) {
                    connection.disconnect();
                } catch (Throwable e) {
                    boolean isExpected = e instanceof IOException && e.getMessage().equals("Broken pipe");
                    if (!isExpected) MinecraftServer.getExceptionManager().handleException(e);
                    connection.disconnect();
                }
                if (!connection.isOnline()) {
                    try {
                        connection.flushSync();
                    } catch (IOException _) {
                        // Ignore IO errors
                    } finally {
                        try {
                            connection.getChannel().close();
                        } catch (IOException _) {
                            // May error if it was disconnect client side
                        }
                    }
                    break; // Disconnect
                }
            }
        } finally {
            connection.cleanup(); // Cleanup pooling
        }
    }

    public boolean isOpen() {
        return !stop;
    }

    public void stop() {
        this.stop = true;
        try {
            final var serverSocket = this.serverSocket;
            if (serverSocket != null) {
                serverSocket.close();
            }

            if (socketAddress instanceof UnixDomainSocketAddress unixDomainSocketAddress) {
                Files.deleteIfExists(unixDomainSocketAddress.getPath());
            }
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    @ApiStatus.Internal
    public PacketParser.Client packetParser() {
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
}
