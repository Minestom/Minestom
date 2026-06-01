package net.minestom.server.instance.light;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public record NeighborSnapshot(Map<Neighbors, LightingChunk> neighbors) {
    public @Nullable LightingChunk get(Neighbors neighbor) {
        return neighbors.get(neighbor);
    }
}
