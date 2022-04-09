package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

import java.util.*;
import java.util.function.Consumer;

import static net.minestom.server.utils.chunk.ChunkUtils.forChunksInRange;
import static net.minestom.server.utils.chunk.ChunkUtils.getChunkIndex;

final class ChunkView {
    private final Instance instance;
    private final Point point;
    final Set<Player> set = new SetImpl();

    private int lastReferenceCount;

    ChunkView(Instance instance, Point point) {
        this.instance = instance;
        this.point = point;
    }

    private Collection<Player> references() {
        final Instance instance = this.instance;
        final Point point = this.point;

        Int2ObjectOpenHashMap<Player> entityMap = new Int2ObjectOpenHashMap<>(lastReferenceCount);

        final List<SharedInstance> shared = instance instanceof InstanceContainer container && !container.getSharedInstances().isEmpty() ?
                container.getSharedInstances() : List.of();
        Long2ObjectSyncMap<List<Entity>>[] entries = new Long2ObjectSyncMap[1 + shared.size()];
        entries[0] = entities(instance);
        for (int i = 0; i < shared.size(); i++) {
            entries[i + 1] = entities(shared.get(i));
        }

        forChunksInRange(point, MinecraftServer.getChunkViewDistance(),
                (chunkX, chunkZ) -> {
                    final long index = getChunkIndex(chunkX, chunkZ);
                    for (var entry : entries) {
                        var chunkEntities = entry.get(index);
                        if (chunkEntities != null && !chunkEntities.isEmpty()) {
                            for (var player : chunkEntities) {
                                entityMap.putIfAbsent(player.getEntityId(), (Player) player);
                            }
                        }
                    }
                });
        this.lastReferenceCount = entityMap.size();
        return entityMap.values();
    }

    final class SetImpl extends AbstractSet<Player> {
        @Override
        public @NotNull Iterator<Player> iterator() {
            return references().iterator();
        }

        @Override
        public int size() {
            return references().size();
        }

        @Override
        public void forEach(Consumer<? super Player> action) {
            references().forEach(action);
        }
    }

    static Long2ObjectSyncMap<List<Entity>> entities(Instance instance) {
        final EntityTrackerImpl tracker = (EntityTrackerImpl) instance.getEntityTracker();
        return tracker.entries[EntityTracker.Target.PLAYERS.ordinal()].chunkEntities;
    }
}
