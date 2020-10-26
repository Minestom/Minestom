package net.minestom.server.network;

import io.netty.buffer.ByteBuf;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.player.PlayerUtils;
import net.minestom.server.utils.thread.MinestomThread;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * Utils class used to write {@link ServerPacket} in the appropriate thread pool.
 * <p>
 * WARNING: those methods do not guarantee a receive order.
 */
public final class PacketWriterUtils {

    private static final ExecutorService PACKET_WRITER_POOL = new MinestomThread(MinecraftServer.THREAD_COUNT_PACKET_WRITER, MinecraftServer.THREAD_NAME_PACKET_WRITER);

    /**
     * Write the {@link ServerPacket} in the writer thread pool.
     * <p>
     * WARNING: should not be used if the packet receive order is important
     *
     * @param serverPacket the packet to write
     * @param consumer     the consumer called once the packet has been written
     */
    public static void writeCallbackPacket(@NotNull ServerPacket serverPacket, @NotNull Consumer<ByteBuf> consumer) {
        PACKET_WRITER_POOL.execute(() -> {
            final ByteBuf buffer = PacketUtils.writePacket(serverPacket);
            consumer.accept(buffer);
        });
    }

    /**
     * Write a {@link ServerPacket} in the writer thread pool and send it to every players in {@code players}.
     * <p>
     * WARNING: should not be used if the packet receive order is important
     *
     * @param players      the players list to send the packet to
     * @param serverPacket the packet to write and send
     */
    public static void writeAndSend(@NotNull Collection<Player> players, @NotNull ServerPacket serverPacket) {
        PACKET_WRITER_POOL.execute(() -> {
            if (players.isEmpty())
                return;

            final ByteBuf buffer = PacketUtils.writePacket(serverPacket);
            for (Player player : players) {
                final PlayerConnection playerConnection = player.getPlayerConnection();
                if (PlayerUtils.isNettyClient(player)) {
                    playerConnection.writePacket(buffer, true);
                } else {
                    playerConnection.sendPacket(serverPacket);
                }
            }
            buffer.release();
        });
    }

    /**
     * Write a {@link ServerPacket} and send it to a {@link PlayerConnection}.
     * <p>
     * WARNING: should not be used if the packet receive order is important
     *
     * @param playerConnection the connection to send the packet to
     * @param serverPacket     the packet to write and send
     */
    public static void writeAndSend(@NotNull PlayerConnection playerConnection, @NotNull ServerPacket serverPacket) {
        PACKET_WRITER_POOL.execute(() -> {
            if (PlayerUtils.isNettyClient(playerConnection)) {
                final ByteBuf buffer = PacketUtils.writePacket(serverPacket);
                buffer.retain();
                playerConnection.writePacket(buffer, false);
                buffer.release();
            } else {
                playerConnection.sendPacket(serverPacket);
            }
        });
    }

    /**
     * Write a {@link ServerPacket} and send it to a {@link Player}.
     * <p>
     * WARNING: should not be used if the packet receive order is important
     *
     * @param player       the player to send the packet to
     * @param serverPacket the packet to write and send
     */
    public static void writeAndSend(@NotNull Player player, @NotNull ServerPacket serverPacket) {
        final PlayerConnection playerConnection = player.getPlayerConnection();
        writeAndSend(playerConnection, serverPacket);
    }

}
