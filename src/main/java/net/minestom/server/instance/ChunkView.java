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

        final List<Instance> instances;
        if (instance instanceof InstanceContainer container && !container.getSharedInstances().isEmpty()) {
            instances = new ArrayList<>(container.getSharedInstances().size() + 1);
            instances.add(instance);
            instances.addAll(container.getSharedInstances());
        } else {
            instances = Collections.singletonList(instance);
        }

        for (Instance inst : instances) {
            inst.getEntityTracker().nearbyEntitiesByChunkRange(point, MinecraftServer.getChunkViewDistance(),
                    EntityTracker.Target.PLAYERS, (player) -> entityMap.putIfAbsent(player.getEntityId(), player));
        }

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
