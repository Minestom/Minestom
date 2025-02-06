package net.minestom.server.entity;

import java.util.List;
import java.util.UUID;

import static net.minestom.server.entity.EntityQuery.*;
import static net.minestom.server.entity.EntityQuery.Condition.*;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

public final class EntityQueries {
    public static @NotNull EntityQuery all() {
        return entityQuery(List.of());
    }

    public static @NotNull EntityQuery players() {
        return entityQuery(Target.ALL_PLAYERS, Sort.ARBITRARY, -1, List.of());
    }

    public static @NotNull EntityQuery name(@NotNull String name) {
        return entityQuery(equalsCondition(NAME, name));
    }

    public static @NotNull EntityQuery id(int id) {
        return entityQuery(equalsCondition(ID, id));
    }

    public static @NotNull EntityQuery uuid(@NotNull UUID uuid) {
        return entityQuery(equalsCondition(UUID, uuid));
    }

    public static @NotNull EntityQuery nearby(double range) {
        return entityQuery(lowerEqualsCondition(DISTANCE, range));
    }

    public static @NotNull EntityQuery nearbyByChunkRange(int range) {
        return entityQuery(chunkRangeCondition(range));
    }

    public static @NotNull EntityQuery atChunk(int chunkX, int chunkZ) {
        return entityQuery(equalsCondition(CHUNK_X, chunkX), equalsCondition(CHUNK_Z, chunkZ));
    }

    public static @NotNull EntityQuery atChunk(Point point) {
        return atChunk(point.chunkX(), point.chunkZ());
    }
}
