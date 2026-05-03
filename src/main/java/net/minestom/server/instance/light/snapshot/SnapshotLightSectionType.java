package net.minestom.server.instance.light.snapshot;

import net.minestom.server.instance.Section;
import net.minestom.server.instance.light.LightSectionType;
import net.minestom.server.instance.light.LightingChunk;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SnapshotLightSectionType implements LightSectionType<SnapshotLightSection, ChunkData> {
    public static final SnapshotLightSectionType TYPE = new SnapshotLightSectionType();

    @Override
    public void fullRelightSync(Collection<? extends SnapshotLightSection> lightSections) {
        for (var lightSection : lightSections) {
            lightSection.fullRelightSync();
        }
    }

    @Override
    public ChunkData newChunkData(LightingChunk chunk) {
        return new ChunkData(chunk);
    }

    @Override
    public SnapshotLightSection newSection(ChunkData chunkData, @Nullable Section section, int sectionY) {
        return new SnapshotLightSection(chunkData, section, sectionY);
    }
}
