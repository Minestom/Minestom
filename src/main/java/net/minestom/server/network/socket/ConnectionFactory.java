package net.minestom.server.network.socket;

import net.minestom.server.network.player.PlayerSocketConnection;

import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

@FunctionalInterface
public interface ConnectionFactory {
    /**
     * Creates the connection for an accepted client.
     * The given threads are unstarted; they will be started by the server after this call.
     *
     * @return the connection to use, or {@code null} to reject the client (its channel will be closed)
     */
    @Nullable PlayerSocketConnection create(SocketChannel channel, SocketAddress remoteAddress,
                                  Thread readThread, Thread writeThread);
}

