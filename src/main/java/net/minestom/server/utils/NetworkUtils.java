package net.minestom.server.utils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Network related utilities.
 */
public class NetworkUtils {

    private NetworkUtils() { }

    /**
     * Gets a free port.
     *
     * @return the port
     * @throws IOException if a port could not be found
     */
    public static int getFreePort() throws IOException {
        int port;

        final ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();

        socket.close();

        return port;
    }
}
