package net.minestom.server.network.socket;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.player.PlayerSocketConnection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;

public final class Server {
    private volatile boolean stop;

    private final PacketParser<ClientPacket> packetParser;

    private ServerSocketChannel serverSocket;
    private SocketAddress socketAddress;
    private String address;
    private int port;

    public Server(PacketParser<ClientPacket> packetParser) {
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
        Thread.startVirtualThread(() -> {
            while (!stop) {
                try {
                    final SocketChannel client = serverSocket.accept();
                    configureSocket(client);
                    PlayerSocketConnection connection = new PlayerSocketConnection(client, client.getRemoteAddress());
                    Thread.startVirtualThread(() -> playerReadLoop(connection));
                    Thread.startVirtualThread(() -> playerWriteLoop(connection));
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
        while (!stop) {
            try {
                // Read & process packets
                connection.read(packetParser);
            } catch (EOFException e) {
                connection.disconnect();
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
        while (!stop) {
            try {
                connection.flushSync();
            } catch (EOFException e) {
                connection.disconnect();
                break;
            } catch (Throwable e) {
                boolean isExpected = e instanceof IOException && e.getMessage().equals("Broken pipe");
                if (!isExpected) MinecraftServer.getExceptionManager().handleException(e);

                connection.disconnect();
                break;
            }
            if (!connection.isOnline()) {
                try {
                    connection.flushSync();
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
    public @NotNull PacketParser<ClientPacket> packetParser() {
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
