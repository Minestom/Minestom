package fr.themode.minestom.net;

import fr.adamaq01.ozao.net.Buffer;
import fr.adamaq01.ozao.net.packet.Packet;
import fr.themode.minestom.Main;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.PacketUtils;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PacketWriterUtils {

    private static volatile ExecutorService batchesPool = Executors.newFixedThreadPool(Main.THREAD_COUNT_PACKET_WRITER);

    public static void writeCallbackPacket(ServerPacket serverPacket, Consumer<Buffer> consumer) {
        batchesPool.execute(() -> {
            Packet p = PacketUtils.writePacket(serverPacket);
            consumer.accept(PacketUtils.encode(p));
        });
    }

    public static void writeAndSend(Collection<Player> players, ServerPacket serverPacket) {
        batchesPool.execute(() -> {
            Packet p = PacketUtils.writePacket(serverPacket);
            Buffer encoded = PacketUtils.encode(p);


            int size = players.size();
            if (size == 0)
                return;
            encoded.getData().retain(size).markReaderIndex();
            for (Player player : players) {
                player.getPlayerConnection().writeUnencodedPacket(encoded);
                encoded.getData().resetReaderIndex();
            }
        });
    }

}
