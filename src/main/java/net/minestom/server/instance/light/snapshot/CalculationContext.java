package net.minestom.server.instance.light.snapshot;

import net.minestom.server.instance.light.LightingChunk;
import net.minestom.server.instance.light.Neighbors;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class CalculationContext {
    private static final Neighbors[] NEIGHBORS = Neighbors.values();
    private final ChunkContext originChunk;
    private final int sectionY;
    private final Map<Neighbors, ChunkContext> neighbors;
    private final int version;

    public CalculationContext(LightingChunk chunk, int sectionY, int version) {
        this.version = version;
        var neighborSnapshot = chunk.createNeighborSnapshot();
        var map = new EnumMap<Neighbors, ChunkContext>(Neighbors.class);
        for (var neighbor : NEIGHBORS) {
            var c = neighborSnapshot.get(neighbor);
            if (c == null) continue;
            map.put(neighbor, create(c, neighbor, sectionY));
        }
        this.originChunk = create(chunk, null, sectionY);
        this.sectionY = sectionY;
        this.neighbors = Map.copyOf(map);
    }

    public int version() {
        return version;
    }

    public ChunkContext originChunk() {
        return originChunk;
    }

    public int sectionY() {
        return sectionY;
    }

    public Map<Neighbors, ChunkContext> neighbors() {
        return neighbors;
    }

    private static ChunkContext create(LightingChunk chunk, @Nullable Neighbors neighbor, int sectionY) {
        var minSection = chunk.getMinSection();
        var maxSection = chunk.getMaxSection();
        var lowerSection = sectionY == minSection - 1 ? null : (SnapshotLightSection) chunk.getLightSection(sectionY - 1);
        var middleSection = (SnapshotLightSection) chunk.getLightSection(sectionY);
        var upperSection = sectionY == maxSection ? null : (SnapshotLightSection) chunk.getLightSection(sectionY + 1);
        var lower = lowerSection == null ? null : lowerSection.snapshot();
        var middle = middleSection.snapshot();
        var upper = upperSection == null ? null : upperSection.snapshot();
        return new ChunkContext(chunk, neighbor, lower, middle, upper);
    }

    public record ChunkContext(LightingChunk chunk,
                               @Nullable Neighbors neighbor,
                               @Nullable SectionSnapshot lower,
                               SectionSnapshot middle,
                               @Nullable SectionSnapshot upper) {
    }
}
