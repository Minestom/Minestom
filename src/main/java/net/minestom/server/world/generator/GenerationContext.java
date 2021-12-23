package net.minestom.server.world.generator;

import com.github.benmanes.caffeine.cache.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.generator.stages.pregeneration.PreGenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.StageData;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GenerationContext {
    private final WorldGenerator worldGenerator;
    private final Instance instance;
    private final Map<Class<? extends PreGenerationStage<?>>, PreGenerationStage<?>> preGenerationStageMap;

    private final LoadingCache<PreGenerationStage<?>, StageData> instanceStageDataMap;
    private final Map<PreGenerationStage<?>, LoadingCache<Long, StageData>> chunkStageDataMap;
    private final Map<PreGenerationStage<?>, LoadingCache<SectionKey, StageData>> sectionStageDataMap;

    public GenerationContext(WorldGenerator worldGenerator, Instance instance, List<PreGenerationStage<?>> preGenerationStages) {
        this.worldGenerator = worldGenerator;
        this.instance = instance;
        preGenerationStageMap = new HashMap<>();
        chunkStageDataMap = new HashMap<>();
        sectionStageDataMap = new HashMap<>();
        instanceStageDataMap = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(60))
                .removalListener((RemovalListener<PreGenerationStage<?>, StageData>) (key, value, cause) -> {
                    if (key == null || value == null) return;
                    final BinaryWriter writer = new BinaryWriter();
                    value.write(writer);
                    instance.getWorldGenDataLoader().saveInstanceData(key.getUniqueId(),
                            key.getVersion(), writer);
                })
                .build(key -> {
                    final BinaryReader reader = instance.getWorldGenDataLoader().readInstanceData(key.getUniqueId(), key.getVersion());
                    return reader == null ? null : key.getDataReader().apply(reader);
                });
        for (PreGenerationStage<?> stage : preGenerationStages) {
            preGenerationStageMap.put((Class<? extends PreGenerationStage<? extends StageData>>) stage.getClass(), stage);
            chunkStageDataMap.put(stage, Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofSeconds(60))
                    .removalListener((RemovalListener<Long, StageData>) (key, value, cause) -> {
                        if (key == null || value == null) return;
                        final BinaryWriter writer = new BinaryWriter();
                        value.write(writer);
                        instance.getWorldGenDataLoader().saveChunkData(stage.getUniqueId(), stage.getVersion(),
                                ChunkUtils.getChunkCoordX(key), ChunkUtils.getChunkCoordZ(key),
                                writer);
                    })
                    .build(key -> {
                        final BinaryReader reader = instance.getWorldGenDataLoader()
                                .readChunkData(stage.getUniqueId(), stage.getVersion(),
                                        ChunkUtils.getChunkCoordX(key), ChunkUtils.getChunkCoordZ(key));
                        return reader == null ? null : stage.getDataReader().apply(reader);
                    }));
            sectionStageDataMap.put(stage, Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofSeconds(60))
                    .removalListener((RemovalListener<SectionKey, StageData>) (key, value, cause) -> {
                        if (key == null || value == null) return;
                        final BinaryWriter writer = new BinaryWriter();
                        value.write(writer);
                        instance.getWorldGenDataLoader().saveSectionData(stage.getUniqueId(), stage.getVersion(),
                                key.x, key.y, key.z,
                                writer);
                    })
                    .build(key -> {
                        final BinaryReader reader = instance.getWorldGenDataLoader()
                                .readSectionData(stage.getUniqueId(), stage.getVersion(), key.x, key.y, key.z);
                        return reader == null ? null : stage.getDataReader().apply(reader);
                    }));
        }
    }

    public <T extends StageData.Instance> @Nullable T getInstanceData(Class<? extends PreGenerationStage<T>> stage) {
        return (T) instanceStageDataMap.get(preGenerationStageMap.get(stage));
    }

    public <T extends StageData.Chunk> @Nullable T getChunkData(Class<? extends PreGenerationStage<T>> stage, int chunkX, int chunkZ) {
        return (T) chunkStageDataMap.get(preGenerationStageMap.get(stage)).get(ChunkUtils.getChunkIndex(chunkX, chunkZ));
    }

    public <T extends StageData.Section> @Nullable T getSectionData(Class<? extends PreGenerationStage<T>> stage, int sectionX, int sectionY, int sectionZ) {
        return (T) sectionStageDataMap.get(preGenerationStageMap.get(stage)).get(new SectionKey(sectionX, sectionY, sectionZ));
    }

    public <T extends StageData.Instance> void setInstanceData(Class<? extends PreGenerationStage<T>> stage, T data) {
        instanceStageDataMap.put(preGenerationStageMap.get(stage), data);
    }

    public <T extends StageData.Chunk> void setChunkData(Class<? extends PreGenerationStage<T>> stage, T data, int chunkX, int chunkZ) {
        chunkStageDataMap.get(preGenerationStageMap.get(stage)).put(ChunkUtils.getChunkIndex(chunkX, chunkZ), data);
    }

    public <T extends StageData.Section> void setSectionData(Class<? extends PreGenerationStage<T>> stage, T data, int sectionX, int sectionY, int sectionZ) {
        sectionStageDataMap.get(preGenerationStageMap.get(stage)).put(new SectionKey(sectionX, sectionY, sectionZ), data);
    }

    public Instance getInstance() {
        return instance;
    }

    public WorldGenerator getWorldGenerator() {
        return worldGenerator;
    }

    private record SectionKey(int x, int y, int z) {}
}
