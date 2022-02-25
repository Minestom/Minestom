package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

final class ChunkView {
    private static final int RANGE = MinecraftServer.getChunkViewDistance() * 16;
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
        final var target = EntityTracker.Target.PLAYERS;

        Int2ObjectOpenHashMap<Player> entityMap = new Int2ObjectOpenHashMap<>(lastReferenceCount);
        // Current Instance
        instance.getEntityTracker().nearbyEntities(point, RANGE, target,
                (entity) -> entityMap.putIfAbsent(entity.getEntityId(), entity));
        // Shared Instances
        if (instance instanceof InstanceContainer container) {
            final List<SharedInstance> shared = container.getSharedInstances();
            if (!shared.isEmpty()) {
                for (var sharedInstance : shared) {
                    sharedInstance.getEntityTracker().nearbyEntities(point, RANGE, target,
                            (entity) -> entityMap.putIfAbsent(entity.getEntityId(), entity));
                }
            }
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
}
