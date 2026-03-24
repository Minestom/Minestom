package net.minestom.server.network.socket;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

public final class Server {
    private volatile boolean stop;

    private final PacketParser<ClientPacket> packetParser;
    private final PacketParser<ServerPacket> packetWriter;

    private @UnknownNullability ServerSocketChannel serverSocket;
    private @UnknownNullability SocketAddress socketAddress;
    private @UnknownNullability String address;
    private int port;

    public Server(PacketParser<ClientPacket> packetParser, PacketParser<ServerPacket> packetWriter) {
        this.packetParser = packetParser;
        this.packetWriter = packetWriter;
    }

    public Server() {
        this(PacketVanilla.CLIENT_PACKET_PARSER, PacketVanilla.SERVER_PACKET_PARSER);
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
        // Use named thread builders for logging
        var readBuilder = Thread.ofVirtual().name("Ms-Socket-Reader-", 0);
        var writeBuilder = Thread.ofVirtual().name("Ms-Socket-Writer-", 0);
        Thread.ofVirtual().name("Ms-Socket-Server").start(() -> serverAcceptLoop(serverSocket, readBuilder, writeBuilder));
    }

    private void serverAcceptLoop(ServerSocketChannel serverSocket, Thread.Builder readBuilder, Thread.Builder writeBuilder) {
        Check.notNull(serverSocket, "serverSocket cannot be null");
        while (!stop) {
            // Handle server IO exceptions.
            final SocketChannel client;
            try {
                client = serverSocket.accept();
            } catch (ClosedChannelException _) {
                // We are exiting, bye bye!
                break;
            } catch (NotYetBoundException ex) {
                throw new IllegalStateException("Server socket is not bound yet", ex);
            } catch (IOException e) {
                // Client bound failed.
                if (!ServerFlag.SUPPRESS_CONNECTION_ACCEPT_ERRORS)
                    MinecraftServer.getExceptionManager().handleException(e);
                continue;
            }
            // Handle client IO exceptions.
            final SocketAddress remoteAddress;
            try {
                configureSocket(client);
                remoteAddress = client.getRemoteAddress();
            } catch (IOException e) {
                try {
                    client.close();
                } catch (IOException _) {}
                // Client rejects changing settings here, could've closed.
                if (!ServerFlag.SUPPRESS_CONNECTION_ACCEPT_ERRORS)
                    MinecraftServer.getExceptionManager().handleException(e);
                continue;
            }
            // Start accepting the player by starting the read and write threads.
            AtomicReference<@UnknownNullability PlayerSocketConnection> reference = new AtomicReference<>(null);
            try {
                Thread readThread = readBuilder.unstarted(() -> playerReadLoop(reference.get()));
                Thread writeThread = writeBuilder.unstarted(() -> playerWriteLoop(reference.get()));
                PlayerSocketConnection connection = new PlayerSocketConnection(client, remoteAddress, readThread, writeThread);
                reference.set(connection);
                readThread.start();
                writeThread.start();
            } catch (Throwable e) {
                MinecraftServer.getExceptionManager().handleException(e);
                try {
                    client.close();
                } catch (IOException _) {
                }
                // Mark for disconnection in the read/write loops.
                // Should ensure that isOnline also returns false.
                PlayerSocketConnection connection = reference.get();
                if (connection != null) connection.disconnect();
            }
        }
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
        Check.notNull(connection, "connection cannot be null");
        final PacketParser<ClientPacket> packetParser = this.packetParser;
        while (!stop) {
            try {
                // Read & process packets
                connection.read(packetParser);
            } catch (ClosedChannelException _) {
                break; // We closed the socket during read, just exit.
            } catch (EOFException _) {
                connection.disconnect();
                break;
            } catch (SocketException e) {
                boolean isExpected = e.getMessage().equals("Connection reset");
                if (!isExpected) MinecraftServer.getExceptionManager().handleException(e);
                connection.disconnect();
                break;
            } catch (Throwable e) {
                MinecraftServer.getExceptionManager().handleException(e);
                connection.disconnect();
                break;
            }
            // Connection was disconnected
            if (!connection.isOnline()) break;
        }
        // Ensure the write thread gets unlocked once the read thread stops.
        connection.unlockWriteThread();
    }

    private void playerWriteLoop(PlayerSocketConnection connection) {
        Check.notNull(connection, "connection cannot be null");
        final PacketParser<ServerPacket> packetWriter = this.packetWriter;
        while (!stop) {
            try {
                connection.awaitFlush();
                connection.flushSync(packetWriter);
            } catch (ClosedChannelException _) {
                break; // We closed the socket during write, just exit.
            } catch (EOFException _) {
                connection.disconnect();
                break;
            } catch (IOException e) {
                boolean isExpected = e.getMessage().equals("Broken pipe") || e.getMessage().equals("Connection reset by peer");
                if (!isExpected) MinecraftServer.getExceptionManager().handleException(e);
                connection.disconnect();
                break;
            } catch (Throwable e) {
                MinecraftServer.getExceptionManager().handleException(e);
                connection.disconnect();
                break;
            }
            if (!connection.isOnline()) {
                try {
                    connection.flushSync(packetWriter);
                    connection.getChannel().close();
                    break;
                } catch (IOException e) {
                    // Disconnect
                    break;
                }
            }
        }
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
    }

    @ApiStatus.Internal
    public PacketParser<ClientPacket> packetParser() {
        return packetParser;
    }

    @ApiStatus.Internal
    public PacketParser<ServerPacket> packetWriter() {
        return packetWriter;
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
