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

    private final Cache<Class<? extends StageData.Instance>, StageData> instanceStageDataMap;
    private final Map<Class<? extends StageData.Chunk>, Cache<Long, StageData>> chunkStageDataMap;
    private final Map<Class<? extends StageData.Section>, Cache<SectionKey, StageData>> sectionStageDataMap;

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
        chunkStageDataMap = new HashMap<>();
        sectionStageDataMap = new HashMap<>();
        instanceStageDataMap = Caffeine.newBuilder().expireAfterAccess(instanceDuration).build();
        for (PreGenerationStage<?> stage : preGenerationStages) {
            if (stage.getType() == PreGenerationStage.Type.CHUNK) {
                //noinspection unchecked
                chunkStageDataMap.put((Class<? extends StageData.Chunk>) stage.getDataClass(), Caffeine.newBuilder().expireAfterAccess(chunkDuration).build());
            } else if (stage.getType() == PreGenerationStage.Type.SECTION) {
                //noinspection unchecked
                sectionStageDataMap.put((Class<? extends StageData.Section>) stage.getDataClass(), Caffeine.newBuilder().expireAfterAccess(sectionDuration).build());
            }
        }
    }

    @Override
    public <T extends StageData.Instance> @Nullable T getInstanceData(Class<T> stageData) {
        //noinspection unchecked
        return (T) instanceStageDataMap.getIfPresent(stageData);
    }

    @Override
    public <T extends StageData.Chunk> @Nullable T getChunkData(Class<T> stageData, int chunkX, int chunkZ) {
        //noinspection unchecked
        return (T) chunkStageDataMap.get(stageData).getIfPresent(ChunkUtils.getChunkIndex(chunkX, chunkZ));
    }

    @Override
    public <T extends StageData.Section> @Nullable T getSectionData(Class<T> stageData, int sectionX, int sectionY, int sectionZ) {
        //noinspection unchecked
        return (T) sectionStageDataMap.get(stageData).getIfPresent(new SectionKey(sectionX, sectionY, sectionZ));
    }

    @Override
    public <T extends StageData.Instance> void setInstanceData(T data) {
        instanceStageDataMap.put(data.getClass(), data);
    }

    @Override
    public <T extends StageData.Chunk> void setChunkData(T data, int chunkX, int chunkZ) {
        chunkStageDataMap.get(data.getClass()).put(ChunkUtils.getChunkIndex(chunkX, chunkZ), data);
    }

    @Override
    public <T extends StageData.Section> void setSectionData(T data, int sectionX, int sectionY, int sectionZ) {
        sectionStageDataMap.get(data.getClass()).put(new SectionKey(sectionX, sectionY, sectionZ), data);
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    private record SectionKey(int x, int y, int z) {}
}
