package net.minestom.server.extras.query;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.extras.query.event.BasicQueryEvent;
import net.minestom.server.extras.query.event.FullQueryEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Utility class to manage responses to the GameSpy4 Query Protocol.
 *
 * @see <a href="https://wiki.vg/Query">wiki.vg</a>
 */
public class Query {
    public static final Charset CHARSET = StandardCharsets.ISO_8859_1;
    private static final Logger LOGGER = LoggerFactory.getLogger(Query.class);
    private static final Random RANDOM = new Random();
    private static final Int2ObjectMap<SocketAddress> CHALLENGE_TOKENS = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    private static volatile boolean started;
    private static volatile DatagramSocket socket;
    private static volatile Thread thread;
    private static volatile Task task;

    private Query() {
    }

    /**
     * Starts the query system, responding to queries on a random port, logging if it could not be started.
     *
     * @return the port
     * @throws IllegalArgumentException if the system was already running
     */
    public static int start() {
        if (socket != null) {
            throw new IllegalArgumentException("System is already running");
        } else {
            int port = 0;
            start(port);
            return port;
        }
    }

    /**
     * Starts the query system, responding to queries on a given port, logging if it could not be started.
     *
     * @param port the port
     * @return {@code true} if the query system started successfully, {@code false} otherwise
     */
    public static boolean start(int port) {
        if (socket != null) {
            return false;
        } else {
            try {
                socket = new DatagramSocket(port);
            } catch (SocketException e) {
                LOGGER.warn("Could not open the query port!", e);
                return false;
            }

            thread = new Thread(Query::run);
            thread.start();
            started = true;

            task = MinecraftServer.getSchedulerManager()
                    .buildTask(CHALLENGE_TOKENS::clear)
                    .repeat(30, TimeUnit.SECOND)
                    .schedule();

            return true;
        }
    }

    /**
     * Stops the query system.
     *
     * @return {@code true} if the query system was stopped, {@code false} if it was not running
     */
    public static boolean stop() {
        if (!started) {
            return false;
        } else {
            started = false;

            thread = null;

            socket.close();
            socket = null;

            task.cancel();
            CHALLENGE_TOKENS.clear();

            return true;
        }
    }

    /**
     * Checks if the query system has been started.
     *
     * @return {@code true} if it has been started, {@code false} otherwise
     */
    public static boolean isStarted() {
        return started;
    }

    private static void run() {
        final byte[] buffer = new byte[16];

        while (started) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // try and receive the packet
            try {
                socket.receive(packet);
            } catch (IOException e) {
                if (!started) {
                    LOGGER.error("An error occurred whilst receiving a query packet.", e);
                    continue;
                } else {
                    return;
                }
            }

            // get the contents
            ByteBuffer data = ByteBuffer.wrap(packet.getData());

            // check the magic field
            if ((data.getShort() & 0xFFFF) != 0xFEFD) {
                continue;
            }

            // now check the query type
            byte type = data.get();

            if (type == 9) { // handshake
                int sessionID = data.getInt();
                int challengeToken = RANDOM.nextInt();

                CHALLENGE_TOKENS.put(challengeToken, packet.getSocketAddress());

                // send the response
                final byte[] responseData = NetworkBuffer.makeArray(response -> {
                    response.write(NetworkBuffer.BYTE, (byte) 9);
                    response.write(NetworkBuffer.INT, sessionID);
                    response.write(NetworkBuffer.STRING_TERMINATED, String.valueOf(challengeToken));
                });

                try {
                    socket.send(new DatagramPacket(responseData, responseData.length, packet.getSocketAddress()));
                } catch (IOException e) {
                    if (!started) {
                        LOGGER.error("An error occurred whilst sending a query handshake packet.", e);
                    } else {
                        return;
                    }
                }
            } else if (type == 0) { // stat
                int sessionID = data.getInt();
                int challengeToken = data.getInt();
                SocketAddress sender = packet.getSocketAddress();

                if (CHALLENGE_TOKENS.containsKey(challengeToken) && CHALLENGE_TOKENS.get(challengeToken).equals(sender)) {
                    int remaining = data.remaining();

                    if (remaining == 0) { // basic
                        BasicQueryEvent event = new BasicQueryEvent(sender, sessionID);
                        EventDispatcher.callCancellable(event, () ->
                                sendResponse(event.getQueryResponse(), sessionID, sender));
                    } else if (remaining == 5) { // full
                        FullQueryEvent event = new FullQueryEvent(sender, sessionID);
                        EventDispatcher.callCancellable(event, () ->
                                sendResponse(event.getQueryResponse(), sessionID, sender));
                    }
                }
            }
        }
    }

    private static void sendResponse(@NotNull NetworkBuffer.Writer queryResponse, int sessionID, @NotNull SocketAddress sender) {
        final byte[] responseData = NetworkBuffer.makeArray(buffer -> {
            // header
            buffer.write(NetworkBuffer.BYTE, (byte) 0);
            buffer.write(NetworkBuffer.INT, sessionID);
            // payload
            buffer.write(queryResponse);
        });
        try {
            socket.send(new DatagramPacket(responseData, responseData.length, sender));
        } catch (IOException e) {
            if (!started) {
                LOGGER.error("An error occurred whilst sending a query handshake packet.", e);
            }
        }
    }
}
