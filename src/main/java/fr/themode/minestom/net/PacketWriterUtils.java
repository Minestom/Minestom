package fr.themode.minestom.net;

import com.github.simplenet.packet.Packet;
import fr.themode.minestom.Main;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.PacketUtils;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PacketWriterUtils {

    private static ExecutorService batchesPool = Executors.newFixedThreadPool(Main.THREAD_COUNT_PACKET_WRITER);

    public static void writeCallbackPacket(ServerPacket serverPacket, Consumer<Packet> consumer) {
        batchesPool.execute(() -> {
            PacketUtils.writePacket(serverPacket, packet -> consumer.accept(packet));
        });
    }

    public static void writeAndSend(Collection<Player> players, ServerPacket serverPacket) {
        batchesPool.execute(() -> {
            int size = players.size();
            if (size == 0)
                return;

            PacketUtils.writePacket(serverPacket, packet -> {
                for (Player player : players) {
                    player.getPlayerConnection().writeUnencodedPacket(packet);
                }
            });
        });
    }

    public static void writeAndSend(PlayerConnection playerConnection, ServerPacket serverPacket) {
        batchesPool.execute(() -> {
            PacketUtils.writePacket(serverPacket, packet -> playerConnection.sendPacket(packet));
        });
    }

    public static void writeAndSend(Player player, ServerPacket serverPacket) {
        writeAndSend(player.getPlayerConnection(), serverPacket);
    }

}
