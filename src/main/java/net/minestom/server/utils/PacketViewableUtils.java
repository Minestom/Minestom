package net.minestom.server.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class PacketViewableUtils {
    // Viewable packets
    private static final Cache<Viewable, ViewableStorage> VIEWABLE_STORAGE_MAP = Caffeine.newBuilder().weakKeys().build();

    @ApiStatus.Experimental
    public static void prepareViewablePacket(@NotNull Viewable viewable, @NotNull ServerPacket serverPacket,
                                             @Nullable Entity entity) {
        if (entity != null && !entity.hasPredictableViewers()) {
            // Operation cannot be optimized
            entity.sendPacketToViewers(serverPacket);
            return;
        }
        if (!ServerFlag.VIEWABLE_PACKET) {
            PacketSendingUtils.sendGroupedPacket(viewable.getViewers(), serverPacket, value -> !Objects.equals(value, entity));
            return;
        }
        final Player exception = entity instanceof Player ? (Player) entity : null;
        ViewableStorage storage = VIEWABLE_STORAGE_MAP.get(viewable, (unused) -> new ViewableStorage());
        storage.append(serverPacket, exception);
    }

    @ApiStatus.Internal
    public static void flush() {
        if (ServerFlag.VIEWABLE_PACKET) {
            VIEWABLE_STORAGE_MAP.asMap().entrySet().parallelStream().forEach(entry ->
                    entry.getValue().process(entry.getKey()));
        }
    }

    @ApiStatus.Experimental
    public static void prepareViewablePacket(@NotNull Viewable viewable, @NotNull ServerPacket serverPacket) {
        prepareViewablePacket(viewable, serverPacket, null);
    }

    static final class ViewableStorage {
        static final ObjectPool<NetworkBuffer> POOL = ObjectPool.pool(
                () -> NetworkBuffer.resizableBuffer(ServerFlag.POOLED_BUFFER_SIZE, MinecraftServer.process()),
                NetworkBuffer::clear);
        // Player id -> list of offsets to ignore (32:32 bits)
        private final Int2ObjectMap<LongArrayList> entityIdMap = new Int2ObjectOpenHashMap<>();
        private final NetworkBuffer buffer = POOL.getAndRegister(this);

        private synchronized void append(ServerPacket serverPacket, @Nullable Player exception) {
            final int start = buffer.writeIndex();
            // Viewable storage is only used for play packets, so fine to assume this.
            PacketWriting.writeFramedPacket(ConnectionState.PLAY, buffer, serverPacket, MinecraftServer.getCompressionThreshold());
            final int end = buffer.writeIndex();
            if (exception != null) {
                final long offsets = (long) start << 32 | end & 0xFFFFFFFFL;
                LongList list = entityIdMap.computeIfAbsent(exception.getEntityId(), id -> new LongArrayList());
                list.add(offsets);
            }
        }

        private synchronized void process(Viewable viewable) {
            if (buffer.writeIndex() == 0) return;
            NetworkBuffer copy = buffer.copy(0, buffer.writeIndex());
            viewable.getViewers().forEach(player -> processPlayer(player, copy));
            this.buffer.clear();
            this.entityIdMap.clear();
        }

        private void processPlayer(Player player, NetworkBuffer buffer) {
            final int size = buffer.size();
            final PlayerConnection connection = player.getPlayerConnection();
            final LongArrayList pairs = entityIdMap.get(player.getEntityId());
            if (pairs != null) {
                // Ensure that we skip the specified parts of the buffer
                int lastWrite = 0;
                final long[] elements = pairs.elements();
                for (int i = 0; i < pairs.size(); ++i) {
                    final long offsets = elements[i];
                    final int start = (int) (offsets >> 32);
                    if (start != lastWrite) writeTo(connection, buffer, lastWrite, start - lastWrite);
                    lastWrite = (int) offsets; // End = last 32 bits
                }
                if (size != lastWrite) writeTo(connection, buffer, lastWrite, size - lastWrite);
            } else {
                // Write all
                writeTo(connection, buffer, 0, size);
            }
        }

        private static void writeTo(PlayerConnection connection, NetworkBuffer buffer, int offset, int length) {
            if (connection instanceof PlayerSocketConnection socketConnection) {
                socketConnection.write(buffer, offset, length);
                return;
            }
            // TODO for non-socket connection
        }
    }
}
