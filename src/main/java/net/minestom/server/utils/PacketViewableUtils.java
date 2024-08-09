package net.minestom.server.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.ServerFlag;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.minestom.server.utils.PacketSendingUtils.sendGroupedPacket;

public final class PacketViewableUtils {
    private static final Cache<Viewable, ViewableStorage> VIEWABLE_STORAGE_MAP = Caffeine.newBuilder().weakKeys().build();

    private PacketViewableUtils() {
    }

    @ApiStatus.Experimental
    public static void prepareViewablePacket(@NotNull Viewable viewable, @NotNull ServerPacket serverPacket,
                                             @Nullable Entity entity) {
        if (entity != null && !entity.hasPredictableViewers()) {
            // Operation cannot be optimized
            entity.sendPacketToViewers(serverPacket);
            return;
        }
        if (!ServerFlag.VIEWABLE_PACKET) {
            sendGroupedPacket(viewable.getViewers(), serverPacket, value -> !Objects.equals(value, entity));
            return;
        }
        final Player exception = entity instanceof Player ? (Player) entity : null;
        ViewableStorage storage = VIEWABLE_STORAGE_MAP.get(viewable, (unused) -> new ViewableStorage());
        storage.append(serverPacket, exception);
    }

    @ApiStatus.Experimental
    public static void prepareViewablePacket(@NotNull Viewable viewable, @NotNull ServerPacket serverPacket) {
        prepareViewablePacket(viewable, serverPacket, null);
    }

    @ApiStatus.Internal
    public static void flush() {
        if (ServerFlag.VIEWABLE_PACKET) {
            VIEWABLE_STORAGE_MAP.asMap().entrySet().parallelStream().forEach(entry ->
                    entry.getValue().process(entry.getKey()));
        }
    }

    private static final class ViewableStorage {
        // Player id -> list of offsets to ignore (32:32 bits)
        private final Int2ObjectMap<IntArrayList> entityIdMap = new Int2ObjectOpenHashMap<>();
        private final List<ServerPacket> packets = new ArrayList<>();

        private synchronized void append(ServerPacket serverPacket, @Nullable Player exception) {
            final int index = packets.size();
            this.packets.add(serverPacket);
            if (exception != null) {
                IntList list = entityIdMap.computeIfAbsent(exception.getEntityId(), id -> new IntArrayList());
                list.add(index);
            }
        }

        private synchronized void process(Viewable viewable) {
            viewable.getViewers().forEach(player -> processPlayer(player, packets));
            this.entityIdMap.clear();
        }

        private void processPlayer(Player player, List<ServerPacket> packets) {
            final PlayerConnection connection = player.getPlayerConnection();
            final IntArrayList pairs = entityIdMap.get(player.getEntityId());
            if (pairs != null) {
                // Ensure that we skip the specified parts of the buffer
                final int[] exceptions = pairs.toIntArray();
                connection.sendPackets(packets, exceptions);
            } else {
                for (ServerPacket packet : packets) connection.sendPacket(packet);
            }
        }
    }
}
