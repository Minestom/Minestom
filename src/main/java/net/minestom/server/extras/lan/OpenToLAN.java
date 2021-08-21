package net.minestom.server.extras.lan;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.NetworkUtils;
import net.minestom.server.utils.time.Cooldown;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static net.minestom.server.ping.ServerListPingType.OPEN_TO_LAN;

/**
 * Utility class to manage opening the server to LAN. Note that this <b>doesn't</b> actually
 * open your server to LAN if it isn't already visible to anyone on your local network.
 * Instead it simply sends the packets needed to trick the Minecraft client into thinking
 * that this is a single-player world that has been opened to LANfor it to be displayed on
 * the bottom of the server list.
 *
 * @see <a href="https://wiki.vg/Server_List_Ping#Ping_via_LAN_.28Open_to_LAN_in_Singleplayer.29">wiki.vg</a>
 */
public class OpenToLAN {
    private static final InetSocketAddress PING_ADDRESS = new InetSocketAddress("224.0.2.60", 4445);

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenToLAN.class);

    private static volatile Cooldown eventCooldown;
    private static volatile DatagramSocket socket = null;
    private static volatile DatagramPacket packet = null;
    private static volatile Task task = null;

    private OpenToLAN() {
    }

    /**
     * Opens the server to LAN with the default config.
     *
     * @return {@code true} if it was opened successfully, {@code false} otherwise
     */
    public static boolean open() {
        return open(new OpenToLANConfig());
    }

    /**
     * Opens the server to LAN.
     *
     * @param config the configuration
     * @return {@code true} if it was opened successfully, {@code false} otherwise
     */
    public static boolean open(@NotNull OpenToLANConfig config) {
        Objects.requireNonNull(config, "config");
        if (socket != null) return false;

        int port = config.port;
        if (port == 0) {
            try {
                port = NetworkUtils.getFreePort();
            } catch (IOException e) {
                LOGGER.warn("Could not find an open port!", e);
                return false;
            }
        }

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            LOGGER.warn("Could not bind to the port!", e);
            return false;
        }

        eventCooldown = new Cooldown(config.delayBetweenEvent);
        task = MinecraftServer.getSchedulerManager().buildTask(OpenToLAN::ping)
                .repeat(config.delayBetweenPings)
                .schedule();
        return true;
    }

    /**
     * Closes the server to LAN.
     *
     * @return {@code true} if it was closed, {@code false} if it was already closed
     */
    public static boolean close() {
        if (socket == null) return false;
        task.cancel();
        socket.close();

        task = null;
        socket = null;
        return true;
    }

    /**
     * Checks if the server is currently opened to LAN.
     *
     * @return {@code true} if it is, {@code false} otherwise
     */
    public static boolean isOpen() {
        return socket != null;
    }

    /**
     * Performs the ping.
     */
    private static void ping() {
        if (!MinecraftServer.getServer().isOpen()) return;
        if (packet == null || eventCooldown.isReady(System.currentTimeMillis())) {
            final ServerListPingEvent event = new ServerListPingEvent(OPEN_TO_LAN);
            EventDispatcher.call(event);

            final byte[] data = OPEN_TO_LAN.getPingResponse(event.getResponseData()).getBytes(StandardCharsets.UTF_8);
            packet = new DatagramPacket(data, data.length, PING_ADDRESS);

            eventCooldown.refreshLastUpdate(System.currentTimeMillis());
        }

        try {
            socket.send(packet);
        } catch (IOException e) {
            LOGGER.warn("Could not send Open to LAN packet!", e);
        }
    }
}
