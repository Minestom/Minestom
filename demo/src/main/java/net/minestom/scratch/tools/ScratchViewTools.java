package net.minestom.scratch.tools;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.function.IntegerBiConsumer;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.minestom.scratch.tools.ScratchNetworkTools.NetworkContext;

public final class ScratchViewTools {

    /**
     * Utils to synchronize packets between close entities.
     */
    public static final class Synchronizer {
        private final int viewDistance;
        private final Map<Integer, Entry> entries = new HashMap<>();
        private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();
        private final IntSet entriesChanged = new IntOpenHashSet();

        public Synchronizer(int viewDistance) {
            this.viewDistance = viewDistance;
        }

        public Entry makeEntry(boolean receiver, int id, Point point,
                               Supplier<List<ServerPacket.Play>> initSupplier,
                               Supplier<List<ServerPacket.Play>> destroySupplier) {
            final Entry entry = new Entry(receiver, id, point, initSupplier, destroySupplier);
            this.entries.put(id, entry);
            this.entriesChanged.add(id);
            return entry;
        }

        public void signalAt(Point point, ServerPacket.Play packet) {
            signalAt(point.chunkX(), point.chunkZ(), packet);
        }

        public void signalAt(int chunkX, int chunkZ, ServerPacket.Play packet) {
            Chunk chunk = chunks.get(ChunkUtils.getChunkIndex(chunkX, chunkZ));
            if (chunk != null) chunk.broadcaster.append(packet);
        }

        public void computePackets(BiConsumer<Integer, NetworkContext.Packet> consumer) {
            // Update chunks viewers
            for (int entryId : entriesChanged) {
                Entry entry = entries.get(entryId);
                if (entry == null) continue;
                if (entry.initialized) {
                    final long oldChunkIndex = ChunkUtils.getChunkIndex(entry.oldChunkX, entry.oldChunkZ);
                    Chunk oldChunk = chunks.get(oldChunkIndex);
                    if (oldChunk != null) {
                        oldChunk.viewers.remove(entryId);
                        if (entry.receiver) oldChunk.viewersReceivers.remove(entryId);
                    }
                }
                if (entry.alive) {
                    final long newChunkIndex = ChunkUtils.getChunkIndex(entry.newChunkX, entry.newChunkZ);
                    Chunk newChunk = chunks.computeIfAbsent(newChunkIndex, Chunk::new);
                    newChunk.viewers.add(entryId);
                    if (entry.receiver) newChunk.viewersReceivers.add(entryId);
                }
            }
            // Send init/destroy packets
            for (int entryId : entriesChanged) {
                Entry entry = entries.get(entryId);
                if (entry == null) continue;
                IntegerBiConsumer newCallback = (x, z) -> {
                    Chunk chunk = chunks.computeIfAbsent(ChunkUtils.getChunkIndex(x, z), Chunk::new);
                    for (int viewerId : entry.receiver ? chunk.viewers : chunk.viewersReceivers) {
                        if (viewerId == entryId) continue;
                        final Entry viewer = entries.get(viewerId);
                        if (viewer == null) continue;
                        if (entry.receiver) {
                            final List<ServerPacket.Play> packets = viewer.initSupplier.get();
                            consumer.accept(entryId, new NetworkContext.Packet.PlayList(packets));
                        }
                        if (viewer.receiver) {
                            final List<ServerPacket.Play> packets = entry.initSupplier.get();
                            consumer.accept(viewerId, new NetworkContext.Packet.PlayList(packets));
                        }
                    }
                    if (entry.receiver) chunk.receivers.add(entryId);
                };
                IntegerBiConsumer oldCallback = (x, z) -> {
                    final Chunk chunk = chunks.get(ChunkUtils.getChunkIndex(x, z));
                    if (chunk == null) return;
                    for (int viewerId : entry.receiver ? chunk.viewers : chunk.viewersReceivers) {
                        if (viewerId == entryId) continue;
                        final Entry viewer = entries.get(viewerId);
                        if (viewer == null) continue;
                        if (entry.receiver) {
                            final List<ServerPacket.Play> packets = viewer.destroySupplier.get();
                            consumer.accept(entryId, new NetworkContext.Packet.PlayList(packets));
                        }
                        if (viewer.receiver) {
                            final List<ServerPacket.Play> packets = entry.destroySupplier.get();
                            consumer.accept(viewerId, new NetworkContext.Packet.PlayList(packets));
                        }
                    }
                    if (entry.receiver) chunk.receivers.remove(entryId);
                };
                if (entry.initialized) {
                    if (entry.alive) {
                        ChunkUtils.forDifferingChunksInRange(entry.newChunkX, entry.newChunkZ,
                                entry.oldChunkX, entry.oldChunkZ,
                                viewDistance, newCallback, oldCallback);
                    } else {
                        ChunkUtils.forChunksInRange(entry.newChunkX, entry.newChunkZ, viewDistance, oldCallback);
                    }
                } else {
                    ChunkUtils.forChunksInRange(entry.newChunkX, entry.newChunkZ, viewDistance, newCallback);
                    entry.initialized = true;
                }
                entry.oldChunkX = entry.newChunkX;
                entry.oldChunkZ = entry.newChunkZ;
            }
            // Remove dead entries
            this.entries.values().removeIf(entry -> !entry.alive);
            // Send update packets
            for (Chunk chunk : chunks.values()) {
                try (Broadcaster.Collector collector = chunk.broadcaster.collector()) {
                    final List<ServerPacket.Play> packets = collector.packets();
                    if (packets.isEmpty()) continue;
                    for (int viewerId : chunk.receivers) {
                        final Entry viewer = entries.get(viewerId);
                        if (viewer == null) continue;
                        final int[] exception = collector.exception(viewerId);
                        consumer.accept(viewerId, new NetworkContext.Packet.PlayList(packets, exception));
                    }
                }
            }
            this.entriesChanged.clear();
        }

        public final class Entry {
            private final boolean receiver;
            private final int id;
            private final Supplier<List<ServerPacket.Play>> initSupplier;
            private final Supplier<List<ServerPacket.Play>> destroySupplier;

            private int oldChunkX, oldChunkZ;
            private int newChunkX, newChunkZ;
            private boolean initialized = false;
            private boolean alive = true;

            public Entry(boolean receiver, int id, Point point,
                         Supplier<List<ServerPacket.Play>> initSupplier,
                         Supplier<List<ServerPacket.Play>> destroySupplier) {
                this.receiver = receiver;
                this.id = id;
                this.oldChunkX = point.chunkX();
                this.oldChunkZ = point.chunkZ();
                this.newChunkX = point.chunkX();
                this.newChunkZ = point.chunkZ();
                this.initSupplier = initSupplier;
                this.destroySupplier = destroySupplier;
            }

            public void move(Point point) {
                this.newChunkX = point.chunkX();
                this.newChunkZ = point.chunkZ();
                if (oldChunkX != newChunkX || oldChunkZ != newChunkZ) {
                    entriesChanged.add(id);
                }
            }

            public void signal(ServerPacket.Play packet) {
                signalAt(newChunkX, newChunkZ, packet);
            }

            public void signalAt(int chunkX, int chunkZ, ServerPacket.Play packet) {
                Chunk chunk = chunks.get(ChunkUtils.getChunkIndex(chunkX, chunkZ));
                if (chunk != null) chunk.broadcaster.append(packet, id);
            }

            public void unmake() {
                entriesChanged.add(id);
                this.alive = false;
            }
        }

        private static final class Chunk {
            private final int x, z;
            private final IntSet viewers = new IntOpenHashSet();
            private final IntSet viewersReceivers = new IntOpenHashSet();
            private final IntSet receivers = new IntOpenHashSet();
            private final Broadcaster broadcaster = new Broadcaster();

            public Chunk(long index) {
                this.x = ChunkUtils.getChunkCoordX(index);
                this.z = ChunkUtils.getChunkCoordZ(index);
            }
        }
    }

    /**
     * Utils to broadcast packets to multiple players while ignoring the original sender.
     * <p>
     * Useful for interest management.
     */
    public static final class Broadcaster {
        private final Int2ObjectMap<IntArrayList> entityIdMap = new Int2ObjectOpenHashMap<>();
        private final List<ServerPacket.Play> packets = new ArrayList<>();

        public void append(ServerPacket.Play packet) {
            this.packets.add(packet);
        }

        public void append(ServerPacket.Play packet, int senderId) {
            final int index = packets.size();
            this.packets.add(packet);
            IntArrayList list = entityIdMap.computeIfAbsent(senderId, id -> new IntArrayList());
            list.add(index);
        }

        public Collector collector() {
            return new Collector();
        }

        public final class Collector implements Closeable {
            private final List<ServerPacket.Play> constPackets = List.copyOf(Broadcaster.this.packets);

            public List<ServerPacket.Play> packets() {
                return constPackets;
            }

            public int[] exception(int id) {
                IntArrayList list = Broadcaster.this.entityIdMap.get(id);
                if (list == null) return IntArrays.EMPTY_ARRAY;
                return list.toIntArray();
            }

            @Override
            public void close() {
                Broadcaster.this.packets.clear();
                Broadcaster.this.entityIdMap.clear();
            }
        }
    }
}
