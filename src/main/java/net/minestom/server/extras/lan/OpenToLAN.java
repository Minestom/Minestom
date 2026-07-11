package net.minestom.server.extras.lan;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.Nullable;
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
 * Instead, it simply sends the packets needed to trick the Minecraft client into thinking
 * that this is a single-player world that has been opened to LAN for it to be displayed on
 * the bottom of the server list.
 *
 * @see <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Server_List_Ping#Ping_via_LAN_(Open_to_LAN_in_Singleplayer)">the Minecraft wiki</a>
 */
public final class OpenToLAN {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenToLAN.class);
    private static volatile @Nullable State state = null;

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
    public static synchronized boolean open(OpenToLANConfig config) {
        Objects.requireNonNull(config, "config");
        if (state != null) return false;
        final long eventDelayNanos = config.delayBetweenEvent.toNanos();
        final DatagramSocket socket;
        try {
            socket = new DatagramSocket(config.port);
        } catch (SocketException e) {
            LOGGER.warn("Could not bind to the port!", e);
            return false;
        }
        final Task task;
        try {
            task = MinecraftServer.getSchedulerManager().buildTask(OpenToLAN::ping)
                    .repeat(config.delayBetweenPings)
                    .schedule();
        } catch (RuntimeException exception) {
            socket.close();
            throw exception;
        }
        state = new State(socket, task, eventDelayNanos);
        return true;
    }

    /**
     * Closes the server to LAN.
     *
     * @return {@code true} if it was closed, {@code false} if it was already closed
     */
    public static synchronized boolean close() {
        final State current = state;
        if (current == null) return false;
        state = null;
        current.task.cancel();
        current.socket.close();
        return true;
    }

    /**
     * Checks if the server is currently opened to LAN.
     *
     * @return {@code true} if it is, {@code false} otherwise
     */
    public static boolean isOpen() {
        return state != null;
    }

    private static void ping() {
        final State current = state;
        if (current == null) return;
        Thread.startVirtualThread(() -> {
            try {
                if (state != current || !MinecraftServer.getServer().isOpen()) return;
                final DatagramPacket packet = current.resolvePacket();
                if (state != current) return;
                current.socket.send(packet);
            } catch (IOException e) {
                if (state == current) LOGGER.warn("Could not send Open to LAN packet!", e);
            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        });
    }

    private static final class State {
        private final DatagramSocket socket;
        private final Task task;
        private final long eventDelayNanos;
        private @Nullable Snapshot snapshot;

        private State(DatagramSocket socket, Task task, long eventDelayNanos) {
            this.socket = socket;
            this.task = task;
            this.eventDelayNanos = eventDelayNanos;
            super();
        }

        private synchronized DatagramPacket resolvePacket() {
            final long now = System.nanoTime();
            if (snapshot != null && now - snapshot.timestampNanos < eventDelayNanos) {
                return snapshot.packet;
            }
            final ServerListPingEvent event = new ServerListPingEvent(OPEN_TO_LAN);
            EventDispatcher.call(event);
            final byte[] data = OPEN_TO_LAN.getPingResponse(event.getStatus()).getBytes(StandardCharsets.UTF_8);
            final DatagramPacket packet = new DatagramPacket(data, data.length,
                    new InetSocketAddress("224.0.2.60", 4445));
            this.snapshot = new Snapshot(packet, now);
            return packet;
        }

        private record Snapshot(DatagramPacket packet, long timestampNanos) {
        }
    }
}
