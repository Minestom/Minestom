package net.minestom.scratch.interest;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.ChunkRangeUtils;
import net.minestom.server.coordinate.CoordConversionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.minestom.server.coordinate.CoordConversionUtils.chunkIndex;

public final class Broadcast {
    private final List<ServerPacket.Play> broadcastPackets = new ArrayList<>();
    private final List<World> worlds = new ArrayList<>();

    public World makeWorld(int viewDistance) {
        final World world = new World(viewDistance);
        this.worlds.add(world);
        return world;
    }

    public void broadcast(ServerPacket.Play packet) {
        this.broadcastPackets.add(packet);
    }

    public void process() {
        for (World world : worlds) {
            // Broadcast packets
            if (!broadcastPackets.isEmpty()) {
                for (World.Entry entry : world.entries.values()) {
                    if (!entry.receiver) continue;
                    entry.packetsConsumer.accept(broadcastPackets, IntArrays.EMPTY_ARRAY);
                }
            }
            // View packets
            world.computePackets((id, packets, exceptions) -> {
                World.Entry entry = world.entries.get(id);
                if (packets.size() == exceptions.length) return;
                entry.packetsConsumer.accept(packets, exceptions);
            });
        }
        this.broadcastPackets.clear();
    }

    public static final class World {
        private final int viewDistance;
        private final Int2ObjectMap<Entry> entries = new Int2ObjectOpenHashMap<>();
        private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();
        private final IntSet entriesChanged = new IntOpenHashSet();

        World(int viewDistance) {
            this.viewDistance = viewDistance;
        }

        public Entry makeEntry(int id, Point point,
                               Supplier<List<ServerPacket.Play>> initSupplier,
                               Supplier<List<ServerPacket.Play>> destroySupplier) {
            final Entry entry = new Entry(false, id, point, initSupplier, destroySupplier, null);
            this.entries.put(id, entry);
            this.entriesChanged.add(id);
            return entry;
        }

        public Entry makeReceiver(int id, Point point,
                                  Supplier<List<ServerPacket.Play>> initSupplier,
                                  Supplier<List<ServerPacket.Play>> destroySupplier,
                                  BiConsumer<List<ServerPacket.Play>, int[]> packetsConsumer) {
            final Entry entry = new Entry(true, id, point, initSupplier, destroySupplier, packetsConsumer);
            this.entries.put(id, entry);
            this.entriesChanged.add(id);
            return entry;
        }

        public void signalAt(Point point, ServerPacket.Play packet) {
            signalAt(point.chunkX(), point.chunkZ(), packet);
        }

        public void signalAt(int chunkX, int chunkZ, ServerPacket.Play packet) {
            Chunk chunk = chunks.get(chunkIndex(chunkX, chunkZ));
            if (chunk != null) chunk.broadcaster.append(packet);
        }

        void computePackets(PacketConsumer consumer) {
            // Update chunks viewers
            for (int entryId : entriesChanged) {
                Entry entry = entries.get(entryId);
                if (entry == null) continue;
                if (entry.initialized) {
                    final long oldChunkIndex = chunkIndex(entry.oldChunkX, entry.oldChunkZ);
                    Chunk oldChunk = chunks.get(oldChunkIndex);
                    if (oldChunk != null) {
                        oldChunk.viewers.remove(entryId);
                        if (entry.receiver) oldChunk.viewersReceivers.remove(entryId);
                    }
                }
                if (entry.alive) {
                    final long newChunkIndex = chunkIndex(entry.newChunkX, entry.newChunkZ);
                    Chunk newChunk = chunks.computeIfAbsent(newChunkIndex, Chunk::new);
                    newChunk.viewers.add(entryId);
                    if (entry.receiver) newChunk.viewersReceivers.add(entryId);
                }
            }
            // Send init/destroy packets
            for (int entryId : entriesChanged) {
                Entry entry = entries.get(entryId);
                if (entry == null) continue;
                ChunkRangeUtils.ChunkConsumer newCallback = (x, z) -> {
                    Chunk chunk = chunks.computeIfAbsent(chunkIndex(x, z), Chunk::new);
                    for (int viewerId : entry.receiver ? chunk.viewers : chunk.viewersReceivers) {
                        if (viewerId == entryId) continue;
                        final Entry viewer = entries.get(viewerId);
                        if (viewer == null) continue;
                        if (entry.receiver) {
                            final List<ServerPacket.Play> packets = viewer.initSupplier.get();
                            consumer.accept(entryId, packets);
                        }
                        if (viewer.receiver) {
                            final List<ServerPacket.Play> packets = entry.initSupplier.get();
                            consumer.accept(viewerId, packets);
                        }
                    }
                    if (entry.receiver) chunk.receivers.add(entryId);
                };
                ChunkRangeUtils.ChunkConsumer oldCallback = (x, z) -> {
                    final Chunk chunk = chunks.get(chunkIndex(x, z));
                    if (chunk == null) return;
                    for (int viewerId : entry.receiver ? chunk.viewers : chunk.viewersReceivers) {
                        if (viewerId == entryId) continue;
                        final Entry viewer = entries.get(viewerId);
                        if (viewer == null) continue;
                        if (entry.receiver) {
                            final List<ServerPacket.Play> packets = viewer.destroySupplier.get();
                            consumer.accept(entryId, packets);
                        }
                        if (viewer.receiver) {
                            final List<ServerPacket.Play> packets = entry.destroySupplier.get();
                            consumer.accept(viewerId, packets);
                        }
                    }
                    if (entry.receiver) chunk.receivers.remove(entryId);
                };
                if (entry.initialized) {
                    if (entry.alive) {
                        ChunkRangeUtils.forDifferingChunksInRange(entry.newChunkX, entry.newChunkZ,
                                entry.oldChunkX, entry.oldChunkZ,
                                viewDistance, newCallback, oldCallback);
                    } else {
                        ChunkRangeUtils.forChunksInRange(entry.newChunkX, entry.newChunkZ, viewDistance, oldCallback);
                    }
                } else {
                    ChunkRangeUtils.forChunksInRange(entry.newChunkX, entry.newChunkZ, viewDistance, newCallback);
                    entry.initialized = true;
                }
                entry.oldChunkX = entry.newChunkX;
                entry.oldChunkZ = entry.newChunkZ;
            }
            // Remove dead entries
            this.entries.values().removeIf(entry -> !entry.alive);
            // Send update packets
            for (Chunk chunk : chunks.values()) {
                PacketStore broadcaster = chunk.broadcaster;
                final List<ServerPacket.Play> packets = broadcaster.packets();
                if (packets.isEmpty()) continue;
                for (int viewerId : chunk.receivers) {
                    final Entry viewer = entries.get(viewerId);
                    if (viewer == null) continue;
                    final int[] exception = broadcaster.exception(viewerId);
                    consumer.accept(viewerId, packets, exception);
                }
                broadcaster.clear();
            }
            this.entriesChanged.clear();
        }

        public final class Entry {
            private final boolean receiver;
            private final int id;
            private final Supplier<List<ServerPacket.Play>> initSupplier;
            private final Supplier<List<ServerPacket.Play>> destroySupplier;
            private final BiConsumer<List<ServerPacket.Play>, int[]> packetsConsumer;

            private int oldChunkX, oldChunkZ;
            private int newChunkX, newChunkZ;
            private boolean initialized = false;
            private boolean alive = true;

            public Entry(boolean receiver, int id, Point point,
                         Supplier<List<ServerPacket.Play>> initSupplier,
                         Supplier<List<ServerPacket.Play>> destroySupplier,
                         BiConsumer<List<ServerPacket.Play>, int[]> packetsConsumer) {
                this.receiver = receiver;
                this.id = id;
                this.oldChunkX = point.chunkX();
                this.oldChunkZ = point.chunkZ();
                this.newChunkX = point.chunkX();
                this.newChunkZ = point.chunkZ();
                this.initSupplier = initSupplier;
                this.destroySupplier = destroySupplier;
                this.packetsConsumer = packetsConsumer;
            }

            public void move(Point point) {
                this.oldChunkX = this.newChunkX;
                this.oldChunkZ = this.newChunkZ;
                this.newChunkX = point.chunkX();
                this.newChunkZ = point.chunkZ();
                if (oldChunkX == newChunkX && oldChunkZ == newChunkZ) return;
                entriesChanged.add(id);
            }

            public void signalLocal(ServerPacket.Play packet) {
                Chunk chunk = chunks.get(chunkIndex(newChunkX, newChunkZ));
                if (chunk != null) {
                    if (receiver) chunk.broadcaster.append(packet, id);
                    else chunk.broadcaster.append(packet);
                }
            }

            public void signalLocal(List<ServerPacket.Play> packets) {
                Chunk chunk = chunks.get(chunkIndex(newChunkX, newChunkZ));
                if (chunk == null) return;
                for (ServerPacket.Play packet : packets) {
                    chunk.broadcaster.append(packet, id);
                }
            }

            public void signalLocalSelf(ServerPacket.Play packet) {
                Chunk chunk = chunks.get(chunkIndex(newChunkX, newChunkZ));
                if (chunk != null) chunk.broadcaster.append(packet);
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
            private final PacketStore broadcaster = new PacketStore();

            public Chunk(long index) {
                this.x = CoordConversionUtils.chunkIndexToChunkX(index);
                this.z = CoordConversionUtils.chunkIndexToChunkZ(index);
            }
        }
    }

    private static final class PacketStore {
        private final Int2ObjectMap<IntArrayList> entityIdMap = new Int2ObjectOpenHashMap<>();
        private final List<ServerPacket.Play> packets = new ArrayList<>();

        public void append(ServerPacket.Play packet) {
            this.packets.add(packet);
        }

        public void append(ServerPacket.Play packet, int senderId) {
            final int index = packets.size();
            this.packets.add(packet);
            entityIdMap.computeIfAbsent(senderId, id -> new IntArrayList()).add(index);
        }

        public List<ServerPacket.Play> packets() {
            return List.copyOf(this.packets);
        }

        public int[] exception(int id) {
            IntArrayList list = PacketStore.this.entityIdMap.get(id);
            if (list == null) return IntArrays.EMPTY_ARRAY;
            return list.toIntArray();
        }

        public void clear() {
            this.packets.clear();
            this.entityIdMap.clear();
        }
    }

    @FunctionalInterface
    public interface PacketConsumer {
        void accept(int id, List<ServerPacket.Play> packets, int[] exceptions);

        default void accept(int id, List<ServerPacket.Play> packets) {
            accept(id, packets, IntArrays.EMPTY_ARRAY);
        }
    }
}
