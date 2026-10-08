package net.minestom.server.network.socket;

import net.minestom.server.network.player.PlayerSocketConnection;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Creates {@link PlayerSocketConnection}s for accepted sockets.
 */
public interface ConnectionFactory {
    PlayerSocketConnection create(SocketChannel channel, SocketAddress remoteAddress,
                                  Thread readThread, Thread writeThread);
}
