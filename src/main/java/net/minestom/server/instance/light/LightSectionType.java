package net.minestom.server.instance.light;

import net.minestom.server.instance.Section;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface LightSectionType<LSection extends LightSection<LSection, ?, ChunkData>, ChunkData> {
    void fullRelightSync(Collection<? extends LSection> lightSections);

    ChunkData newChunkData(LightingChunk chunk);

    LSection newSection(ChunkData chunkData, @Nullable Section section, int sectionY);
}
