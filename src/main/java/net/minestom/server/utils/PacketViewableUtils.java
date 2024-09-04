package net.minestom.server.utils;

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
import net.minestom.server.network.packet.server.BufferedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

@ApiStatus.Internal
public final class PacketViewableUtils {
    // Viewable packets
    private static volatile Map<Viewable, ViewableStorage> storageMap = new WeakHashMap<>();

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
        ViewableStorage storage = retrieveStorage(viewable);
        storage.append(serverPacket, exception);
    }

    private static ViewableStorage retrieveStorage(Viewable viewable) {
        Map<Viewable, ViewableStorage> map = storageMap;
        ViewableStorage storage = map.get(viewable);
        if (storage == null) {
            synchronized (PacketViewableUtils.class) {
                map = storageMap;
                storage = map.get(viewable);
                if (storage == null) {
                    storage = new ViewableStorage();
                    map = new WeakHashMap<>(map);
                    map.put(viewable, storage);
                    storageMap = map;
                }
            }
        }
        return storage;
    }

    public static void flush() {
        if (!ServerFlag.VIEWABLE_PACKET) return;
        Map<Viewable, ViewableStorage> map = storageMap;
        map.entrySet().parallelStream().forEach(entry ->
                entry.getValue().process(entry.getKey()));
    }

    public static void prepareViewablePacket(@NotNull Viewable viewable, @NotNull ServerPacket serverPacket) {
        prepareViewablePacket(viewable, serverPacket, null);
    }

    private static final class ViewableStorage {
        private static final ObjectPool<NetworkBuffer> POOL = ObjectPool.pool(
                () -> NetworkBuffer.resizableBuffer(ServerFlag.POOLED_BUFFER_SIZE, MinecraftServer.process()),
                NetworkBuffer::clear);
        // Player id -> list of offsets to ignore (32:32 bits)
        private final Int2ObjectMap<LongArrayList> entityIdMap = new Int2ObjectOpenHashMap<>();
        private final NetworkBuffer buffer = POOL.getAndRegister(this);

        private synchronized void append(ServerPacket serverPacket, @Nullable Player exception) {
            final long start = buffer.writeIndex();
            // Viewable storage is only used for play packets, so fine to assume this.
            PacketWriting.writeFramedPacket(buffer, ConnectionState.PLAY, serverPacket, MinecraftServer.getCompressionThreshold());
            final long end = buffer.writeIndex();
            if (exception != null) {
                final long offsets = start << 32 | end & 0xFFFFFFFFL;
                LongList list = entityIdMap.computeIfAbsent(exception.getEntityId(), id -> new LongArrayList());
                list.add(offsets);
            }
        }

        private synchronized void process(Viewable viewable) {
            if (buffer.writeIndex() == 0) return;
            NetworkBuffer copy = buffer.copy(0, buffer.writeIndex());
            copy.readOnly();
            viewable.getViewers().forEach(player -> processPlayer(player, copy));
            this.buffer.clear();
            this.entityIdMap.clear();
        }

        private void processPlayer(Player player, NetworkBuffer buffer) {
            final long capacity = buffer.capacity();
            final PlayerConnection connection = player.getPlayerConnection();
            final LongArrayList pairs = entityIdMap.get(player.getEntityId());
            if (pairs == null) {
                // No range exception, write the whole buffer
                writeTo(connection, buffer, 0, capacity);
                return;
            }
            // Player has range exception(s)
            // Ensure that we skip the specified parts of the buffer
            int lastWrite = 0;
            final long[] elements = pairs.elements();
            for (int i = 0; i < pairs.size(); ++i) {
                final long offsets = elements[i];
                final int start = (int) (offsets >> 32);
                if (start != lastWrite) writeTo(connection, buffer, lastWrite, start - lastWrite);
                lastWrite = (int) offsets; // End = last 32 bits
            }
            if (capacity != lastWrite) writeTo(connection, buffer, lastWrite, capacity - lastWrite);
        }

        private static void writeTo(PlayerConnection connection, NetworkBuffer buffer, long offset, long length) {
            if (connection instanceof PlayerSocketConnection socketConnection) {
                socketConnection.sendPacket(new BufferedPacket(buffer, offset, length));
                return;
            }
            // TODO for non-socket connection
        }
    }
}
