package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

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
        Int2ObjectOpenHashMap<Player> entityMap = new Int2ObjectOpenHashMap<>(lastReferenceCount);
        collectPlayers(instance, entityMap);
        if (instance instanceof InstanceContainer container && !container.getSharedInstances().isEmpty()) {
            for (Instance shared : container.getSharedInstances()) {
                collectPlayers(shared, entityMap);
            }
        }
        this.lastReferenceCount = entityMap.size();
        return entityMap.values();
    }

    private void collectPlayers(Instance instance, Int2ObjectOpenHashMap<Player> map) {
        instance.getEntityTracker().nearbyEntitiesByChunkRange(point, MinecraftServer.getChunkViewDistance(),
                EntityTracker.Target.PLAYERS, (player) -> map.putIfAbsent(player.getEntityId(), player));
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
