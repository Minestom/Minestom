package net.minestom.server.extras.query;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.query.event.BasicQueryEvent;
import net.minestom.server.extras.query.event.FullQueryEvent;
import net.minestom.server.extras.query.response.QueryResponse;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.NetworkUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Utility class to manage responses to the GameSpy4 Query Protocol.
 * @see <a href="https://wiki.vg/Query">wiki.vg</a>
 */
public class Query {
    public static final Charset CHARSET = StandardCharsets.ISO_8859_1;
    private static final Logger LOGGER = LoggerFactory.getLogger(Query.class);
    private static final Random RANDOM = new Random();

    private static volatile boolean started;

    private static volatile DatagramSocket socket;
    private static volatile Thread thread;

    private static final Int2ObjectMap<SocketAddress> CHALLENGE_TOKENS = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    private static volatile Task task;

    private Query() { }

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
            int port;

            try {
                port = NetworkUtils.getFreePort();
            } catch (IOException e) {
                LOGGER.warn("Could not find an open port!", e);
                return -1;
            }

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

            /*task = MinecraftServer.getSchedulerManager()
                    .buildTask(CHALLENGE_TOKENS::clear)
                    .repeat(30, TimeUnit.SECOND)
                    .schedule();*/

            return true;
        }
    }

    /**
     * Stops the query system.
     *
     * @return {@code true} if the query system was stopped, {@code false} if it was not running
     */
    public boolean stop() {
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
    public boolean isStarted() {
        return started;
    }

    private static void run() {
        final byte[] buffer = new byte[16];

        while (started) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // try and receive the packet
            try {
                System.out.println("WAITING");
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
            ByteBuf data = Unpooled.wrappedBuffer(packet.getData());

            // check the magic field
            if (data.readUnsignedShort() != 0xFEFD) {
                System.out.println("UNKNOWN");
                continue;
            }

            // now check the query type
            byte type = data.readByte();
            System.out.println("READ " + type);

            if (type == 9) { // handshake
                int sessionID = data.readInt();
                int challengeToken = RANDOM.nextInt();

                CHALLENGE_TOKENS.put(challengeToken, packet.getSocketAddress());

                // send the response
                BinaryWriter response = new BinaryWriter(32);
                response.writeByte((byte) 9);
                response.writeInt(sessionID);
                response.writeNullTerminatedString(String.valueOf(challengeToken), CHARSET);

                try {
                    byte[] responseData = response.toByteArray();
                    socket.send(new DatagramPacket(responseData, responseData.length, packet.getSocketAddress()));
                } catch (IOException e) {
                    LOGGER.error("An error occurred whilst sending a query handshake packet.", e);
                }
            } else if (type == 0) { // stat
                int sessionID = data.readInt();
                int challengeToken = data.readInt();
                SocketAddress sender = packet.getSocketAddress();

                System.out.println("  STAT");

                if (CHALLENGE_TOKENS.containsKey(challengeToken) && CHALLENGE_TOKENS.get(challengeToken).equals(sender)) {
                    int remaining = data.readableBytes();

                    System.out.println("  REMAINING " + remaining);

                    if (remaining == 0) { // basic
                        BasicQueryEvent event = new BasicQueryEvent(sender);
                        MinecraftServer.getGlobalEventHandler().callCancellableEvent(BasicQueryEvent.class, event,
                                () -> sendResponse(event.getQueryResponse(), sessionID, sender));
                    } else if (remaining == 5) { // full
                        FullQueryEvent event = new FullQueryEvent(sender);
                        MinecraftServer.getGlobalEventHandler().callCancellableEvent(FullQueryEvent.class, event,
                                () -> sendResponse(event.getQueryResponse(), sessionID, sender));
                    }
                }
            }
        }
    }

    private static void sendResponse(@NotNull QueryResponse queryResponse, int sessionID, @NotNull SocketAddress sender) {
        // header
        BinaryWriter response = new BinaryWriter();
        response.writeByte((byte) 0);
        response.writeInt(sessionID);

        // payload
        queryResponse.write(response);

        // send!
        byte[] responseData = response.toByteArray();
        try {
            socket.send(new DatagramPacket(responseData, responseData.length, sender));

            System.out.println(ByteBufUtil.hexDump(responseData));

            System.out.println("SENT!! basic=" + (queryResponse instanceof BasicQueryEvent));
        } catch (IOException e) {
            LOGGER.error("An error occurred whilst sending a query handshake packet.", e);
        }
    }
}
