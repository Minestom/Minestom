package net.minestom.server.world.generator;

import net.minestom.server.instance.Instance;
import net.minestom.server.world.generator.stages.pregeneration.PreGenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.StageData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface GenerationContext {
    <T extends StageData.Instance> @Nullable T getInstanceData(Class<T> stageData);

    <T extends StageData.Chunk> @Nullable T getChunkData(Class<T> stageData, int chunkX, int chunkZ);

    <T extends StageData.Section> @Nullable T getSectionData(Class<T> stageData, int sectionX, int sectionY, int sectionZ);

    <T extends StageData.Instance> void setInstanceData(T data);

    <T extends StageData.Chunk> void setChunkData(T data, int chunkX, int chunkZ);

    <T extends StageData.Section> void setSectionData(T data, int sectionX, int sectionY, int sectionZ);

    Instance getInstance();

    @FunctionalInterface
    interface Factory {
        GenerationContext newInstance(Instance instance, List<PreGenerationStage<?>> preGenerationStages);
    }

    @FunctionalInterface
    interface Provider {
        GenerationContext getContext(Instance instance);
    }
}
