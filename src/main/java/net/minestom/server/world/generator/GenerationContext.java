package net.minestom.server.world.generator;

import net.minestom.server.instance.Instance;
import net.minestom.server.world.generator.stages.pregeneration.PreGenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.StageData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface GenerationContext {
    <T extends StageData.Instance> @Nullable T getInstanceData(Class<? extends PreGenerationStage<T>> stage);

    <T extends StageData.Chunk> @Nullable T getChunkData(Class<? extends PreGenerationStage<T>> stage, int chunkX, int chunkZ);

    <T extends StageData.Section> @Nullable T getSectionData(Class<? extends PreGenerationStage<T>> stage, int sectionX, int sectionY, int sectionZ);

    <T extends StageData.Instance> void setInstanceData(Class<? extends PreGenerationStage<T>> stage, T data);

    <T extends StageData.Chunk> void setChunkData(Class<? extends PreGenerationStage<T>> stage, T data, int chunkX, int chunkZ);

    <T extends StageData.Section> void setSectionData(Class<? extends PreGenerationStage<T>> stage, T data, int sectionX, int sectionY, int sectionZ);

    Instance getInstance();

    interface Factory {
        GenerationContext newInstance(Instance instance, List<PreGenerationStage<?>> preGenerationStages);
    }
}
