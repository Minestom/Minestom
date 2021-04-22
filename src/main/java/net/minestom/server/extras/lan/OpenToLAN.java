package net.minestom.server.extras.lan;

import java.io.IOException;
import java.net.*;

import java.nio.charset.StandardCharsets;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson;

/**
 * Utility class to manage opening the server to LAN. Note that this doesn't actually
 * open your server to LAN, as it will be visible to anyone on your local network anyway.
 * Instead it simply sends the packets needed to trick the Minecraft client into thinking
 * that this is a server hosted on your LAN in order for it to be displayed on the bottom
 * of the server list.
 * @see <a href="https://wiki.vg/Server_List_Ping#Ping_via_LAN_.28Open_to_LAN_in_Singleplayer.29">wiki.vg</a>
 */
public class OpenToLAN {
    private static final InetSocketAddress PING_ADDRESS = new InetSocketAddress("224.0.2.60", 4445);
    private static final String PING_FORMAT = "[MOTD]%s[/MOTD][AD]%s[/AD]";

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenToLAN.class);

    private static volatile Component description = Component.text("A Minestom server", NamedTextColor.AQUA);
    private static volatile DatagramPacket packet = null;

    private static volatile DatagramSocket socket = null;
    private static volatile Task task = null;

    private OpenToLAN() { }

    /**
     * Opens the server to LAN, printing a warning message if an error occurred.
     *
     * @return {@code true} if it was opened, {@code false} if it was already or couldn't be opened
     * @see OpenToLAN
     */
    public static boolean open() {
        try {
            return openWithException();
        } catch (IOException e) {
            LOGGER.warn("Could not open a socket for LAN connections!", e);
            return false;
        }
    }

    /**
     * Opens the server to LAN, printing a warning message if an error occurred.
     *
     * @param port the port to send the LAN packets from
     * @return {@code true} if it was opened, {@code false} if it was already or couldn't be opened
     * @see OpenToLAN
     */
    public static boolean open(int port) {
        try {
            return openWithException(port);
        } catch (SocketException e) {
            LOGGER.warn("Could not open a socket for LAN connections!", e);
            return false;
        }
    }

    /**
     * Opens the server to LAN.
     *
     * @return {@code true} if it was opened, {@code false} if it was already opened
     * @throws IOException if an error occurred
     * @see OpenToLAN
     */
    public static boolean openWithException() throws IOException {
        if (socket != null) {
            return false;
        } else {
            ServerSocket socket = new ServerSocket(0);
            int port = socket.getLocalPort();
            socket.close();

            return open(port);
        }
    }

    /**
     * Opens the server to LAN.
     *
     * @param port the port to send the LAN packets from
     * @return {@code true} if it was opened, {@code false} if it was already opened
     * @throws SocketException if an error occurred
     * @see OpenToLAN
     */
    public static boolean openWithException(int port) throws SocketException {
        if (socket != null) {
            return false;
        } else {
            socket = new DatagramSocket(port);
            task = MinecraftServer.getSchedulerManager().buildTask(OpenToLAN::ping)
                    .repeat(15, TimeUnit.SECOND)
                    .schedule();
            return true;
        }
    }

    /**
     * Closes the server to LAN.
     *
     * @return {@code true} if it was closed, {@code false} if it was already closed
     */
    public static boolean close() {
        if (socket == null) {
            return false;
        } else {
            task.cancel();
            socket.close();

            task = null;
            socket = null;

            return true;
        }
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
     * Gets the description to be displayed in the server list.
     *
     * @return the description
     */
    public static @NotNull Component getDescription() {
        return description;
    }

    /**
     * Sets the description to be displayed in the server list. Note that the description
     * is sent in the legacy format and will be run through the {@link LegacyComponentSerializer}
     * before being sent to the client.
     *
     * @param component the description
     */
    public static void setDescription(@NotNull Component component) {
        description = Objects.requireNonNull(component, "component cannot be null");
    }

    /**
     * Performs the ping.
     */
    private static void ping() {
        if (MinecraftServer.getNettyServer().getPort() != 0) {
            if (packet == null) {
                generatePacket();
            }

            try {
                socket.send(packet);
            } catch (IOException e) {
                LOGGER.warn("Could not send Open to LAN packet!", e);
            }
        }
    }

    /**
     * Generates the payload packet.
     */
    private static void generatePacket() {
        final byte[] data = String.format(PING_FORMAT, LEGACY_SERIALIZER.serialize(description),
                MinecraftServer.getNettyServer().getPort()).getBytes(StandardCharsets.UTF_8);

        packet = new DatagramPacket(data, data.length, PING_ADDRESS);
    }
}
