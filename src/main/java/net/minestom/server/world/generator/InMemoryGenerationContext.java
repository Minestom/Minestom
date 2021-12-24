package net.minestom.server.world.generator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.generator.stages.pregeneration.PreGenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.StageData;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryGenerationContext implements GenerationContext {
    private final Instance instance;
    private final Map<Class<? extends PreGenerationStage<?>>, PreGenerationStage<? extends StageData>> preGenerationStageMap;

    private final Cache<PreGenerationStage<?>, StageData> instanceStageDataMap;
    private final Map<PreGenerationStage<?>, Cache<Long, StageData>> chunkStageDataMap;
    private final Map<PreGenerationStage<?>, Cache<SectionKey, StageData>> sectionStageDataMap;

    public static GenerationContext.Factory factory() {
        return factoryWithCacheExpirationOf(Duration.ofSeconds(120));
    }

    public static GenerationContext.Factory factoryWithCacheExpirationOf(Duration global) {
        return factoryWithCacheExpirationOf(global, global, global);
    }

    public static GenerationContext.Factory factoryWithCacheExpirationOf(Duration instanceDuration, Duration chunkDuration, Duration sectionDuration) {
        return (instance, preGenerationStages) -> new InMemoryGenerationContext(instance, preGenerationStages, instanceDuration, chunkDuration, sectionDuration);
    }

    private InMemoryGenerationContext(Instance instance, List<PreGenerationStage<?>> preGenerationStages, Duration instanceDuration, Duration chunkDuration, Duration sectionDuration) {
        this.instance = instance;
        preGenerationStageMap = new HashMap<>();
        chunkStageDataMap = new HashMap<>();
        sectionStageDataMap = new HashMap<>();
        instanceStageDataMap = Caffeine.newBuilder().expireAfterAccess(instanceDuration).build();
        for (PreGenerationStage<?> stage : preGenerationStages) {
            //noinspection unchecked
            preGenerationStageMap.put((Class<? extends PreGenerationStage<? extends StageData>>) stage.getClass(), stage);
            chunkStageDataMap.put(stage, Caffeine.newBuilder().expireAfterAccess(chunkDuration).build());
            sectionStageDataMap.put(stage, Caffeine.newBuilder().expireAfterAccess(sectionDuration).build());
        }
    }

    @Override
    public <T extends StageData.Instance> @Nullable T getInstanceData(Class<? extends PreGenerationStage<T>> stage) {
        //noinspection unchecked
        return (T) instanceStageDataMap.getIfPresent(preGenerationStageMap.get(stage));
    }

    @Override
    public <T extends StageData.Chunk> @Nullable T getChunkData(Class<? extends PreGenerationStage<T>> stage, int chunkX, int chunkZ) {
        //noinspection unchecked
        return (T) chunkStageDataMap.get(preGenerationStageMap.get(stage)).getIfPresent(ChunkUtils.getChunkIndex(chunkX, chunkZ));
    }

    @Override
    public <T extends StageData.Section> @Nullable T getSectionData(Class<? extends PreGenerationStage<T>> stage, int sectionX, int sectionY, int sectionZ) {
        //noinspection unchecked
        return (T) sectionStageDataMap.get(preGenerationStageMap.get(stage)).getIfPresent(new SectionKey(sectionX, sectionY, sectionZ));
    }

    @Override
    public <T extends StageData.Instance> void setInstanceData(Class<? extends PreGenerationStage<T>> stage, T data) {
        instanceStageDataMap.put(preGenerationStageMap.get(stage), data);
    }

    @Override
    public <T extends StageData.Chunk> void setChunkData(Class<? extends PreGenerationStage<T>> stage, T data, int chunkX, int chunkZ) {
        chunkStageDataMap.get(preGenerationStageMap.get(stage)).put(ChunkUtils.getChunkIndex(chunkX, chunkZ), data);
    }

    @Override
    public <T extends StageData.Section> void setSectionData(Class<? extends PreGenerationStage<T>> stage, T data, int sectionX, int sectionY, int sectionZ) {
        sectionStageDataMap.get(preGenerationStageMap.get(stage)).put(new SectionKey(sectionX, sectionY, sectionZ), data);
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    private record SectionKey(int x, int y, int z) {}
}
