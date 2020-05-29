package net.minestom.server.network;

import io.netty.buffer.ByteBuf;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.player.PlayerUtils;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class PacketWriterUtils {

    private static ExecutorService batchesPool = new MinestomThread(MinecraftServer.THREAD_COUNT_PACKET_WRITER, MinecraftServer.THREAD_NAME_PACKET_WRITER);

    public static void writeCallbackPacket(ServerPacket serverPacket, Consumer<ByteBuf> consumer) {
        batchesPool.execute(() -> {
            ByteBuf buffer = PacketUtils.writePacket(serverPacket);
            consumer.accept(buffer);
        });
    }

    public static void writeAndSend(Collection<Player> players, ServerPacket serverPacket) {
        batchesPool.execute(() -> {
            if (players.isEmpty())
                return;

            ByteBuf buffer = PacketUtils.writePacket(serverPacket);
            for (Player player : players) {
                PlayerConnection playerConnection = player.getPlayerConnection();
                if (PlayerUtils.isNettyClient(player)) {
                    playerConnection.writePacket(buffer);
                } else {
                    playerConnection.sendPacket(serverPacket);
                }
            }
            buffer.release();
        });
    }

    public static void writeAndSend(PlayerConnection playerConnection, ServerPacket serverPacket) {
        batchesPool.execute(() -> {
            if (PlayerUtils.isNettyClient(playerConnection)) {
                ByteBuf buffer = PacketUtils.writePacket(serverPacket);
                playerConnection.writePacket(buffer);
                buffer.release();
            } else {
                playerConnection.sendPacket(serverPacket);
            }
        });
    }

    public static void writeAndSend(Player player, ServerPacket serverPacket) {
        writeAndSend(player.getPlayerConnection(), serverPacket);
    }

}
