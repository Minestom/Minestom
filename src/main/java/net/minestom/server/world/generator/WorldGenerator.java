package net.minestom.server.world.generator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.world.generator.stages.generation.GenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.PreGenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.StageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class WorldGenerator implements Generator, GenerationContext.Provider {
    private final static Logger LOGGER = LoggerFactory.getLogger(WorldGenerator.class);
    private final Cache<StageKey, CompletableFuture<Void>> preGenStages = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .build();
    //TODO Do we need to care about de-duplicating section requests?
    private final Cache<SectionKey, CompletableFuture<Void>> sectionGens = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .build();
    private final List<PreGenerationStage<?>> preGenerationStages;
    private final List<GenerationStage> generationStages;
    private final GenerationContext.Factory generationContextFactory;

    public WorldGenerator(List<GenerationStage> generationStages) {
        this(Collections.emptyList(), generationStages, null);
    }

    public WorldGenerator(List<PreGenerationStage<?>> preGenerationStages, List<GenerationStage> generationStages, GenerationContext.Factory generationContextFactory) {
        this.generationContextFactory = generationContextFactory;
        this.generationStages = Collections.unmodifiableList(generationStages);
        final Comparator<PreGenerationStage<?>> type = Comparator.comparing(PreGenerationStage::getType);
        final Comparator<PreGenerationStage<?>> range = Comparator.comparing(PreGenerationStage::getRange);
        //TODO Handle dependencies
        this.preGenerationStages = preGenerationStages.stream().sorted(type.thenComparing(range)).toList();
        if (!this.preGenerationStages.equals(preGenerationStages)) {
            LOGGER.warn("Supplied pre-generation stages were not ordered, they have been automatically rearranged!");
        }
    }

    @Override
    public List<CompletableFuture<SectionResult>> generateSections(Instance instance, List<Vec> sections) {
        final ArrayList<CompletableFuture<SectionResult>> futures = new ArrayList<>(sections.size());
        for (final Vec pos : sections) {
            final SectionResult result = new SectionResult(new SectionData(new SectionBlockCache(), Palette.biomes()), pos);
            futures.add(generateSection(instance, result.sectionData().blockCache(), result.sectionData().biomePalette(),
                    (int) pos.x(), (int) pos.y(), (int) pos.z())
                    .thenCompose(unused -> CompletableFuture.completedFuture(result)));
        }
        return futures;
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Void> generateSection(Instance instance, SectionBlockCache blockCache, Palette biomePalette,
                                                    int sectionX, int sectionY, int sectionZ) {
        return sectionGens.get(new SectionKey(instance, new P(sectionX, sectionY, sectionZ)), key -> {
            final GenerationContext context = instance.getGenerationContext();
            final CompletableFuture<Void> completableFuture = new CompletableFuture<>();

            // Pre Gen

            for (PreGenerationStage<? extends StageData> stage : preGenerationStages) {
                CompletableFuture<?>[] futures = switch (stage.getType()) {
                    case INSTANCE -> {
                        final StageData.Instance data = context
                                .getInstanceData(((Class<? extends StageData.Instance>) stage.getDataClass()));
                        yield new CompletableFuture[]{data != null && data.generated() ? AsyncUtils.VOID_FUTURE :
                                preGenStages.get(new StageKey(instance, null, stage), kk -> executePreGenerationStage(context,0, 0, 0, stage))};
                    }
                    case CHUNK -> {
                        CompletableFuture<?>[] f = new CompletableFuture[MathUtils.square(stage.getRange()*2+1)];
                        int i = 0;
                        for (int x = -stage.getRange(); x <= stage.getRange(); x++) {
                            for (int z = -stage.getRange(); z <= stage.getRange(); z++) {
                                final P loc = new P(sectionX + x, 0, sectionZ + z);
                                final StageData.Chunk data = context
                                        .getChunkData(((Class<? extends StageData.Chunk>) stage.getDataClass()), loc.x(), loc.z());
                                if (data != null && data.generated()) {
                                    f[i++] = AsyncUtils.VOID_FUTURE;
                                } else {
                                    f[i++] = preGenStages.get(new StageKey(instance, loc, stage), k ->
                                            executePreGenerationStage(context, k.loc.x(),0, k.loc.z(), stage));
                                }
                            }
                        }
                        yield f;
                    }
                    case SECTION -> {
                        int min = Math.max(sectionY-stage.getRange(), instance.getSectionMinY());
                        int max = Math.min(sectionY+stage.getRange(), instance.getSectionMaxY());
                        CompletableFuture<?>[] f = new CompletableFuture[MathUtils.square(stage.getRange()*2+1) + (max-min)];
                        int i = 0;
                        for (int x = -stage.getRange(); x <= stage.getRange(); x++) {
                            for (int y = min; y < max; y++) {
                                for (int z = -stage.getRange(); z <= stage.getRange(); z++) {
                                    final P loc = new P(sectionX + x, y, sectionZ + z);
                                    final StageData.Section data = context
                                            .getSectionData(((Class<? extends StageData.Section>)
                                                    stage.getDataClass()), loc.x(), loc.y(), loc.z());
                                    if (data != null && data.generated()) {
                                        f[i++] = AsyncUtils.VOID_FUTURE;
                                    } else {
                                        f[i++] = preGenStages.get(new StageKey(instance, loc, stage), k ->
                                                executePreGenerationStage(context, k.loc.x(), k.loc.y(), k.loc.z(), stage));
                                    }
                                }
                            }
                        }
                        yield f;
                    }
                };

                try {
                    CompletableFuture.allOf(futures).get();
                } catch (InterruptedException | ExecutionException e) {
                    //TODO Error handling
                    e.printStackTrace();
                }
            }

            // Gen

            WORLD_GEN_POOL.execute(() -> {
                for (GenerationStage generationStage : generationStages) {
                    generationStage.process(context, blockCache, biomePalette, sectionX, sectionY, sectionZ);
                }
                completableFuture.complete(null);
            });

            return completableFuture;
        });
    }

    private CompletableFuture<Void> executePreGenerationStage(GenerationContext context, int x, int y, int z, PreGenerationStage<?> stage) {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        WORLD_GEN_POOL.execute(() -> {
            stage.process(context, x, y, z);
            completableFuture.complete(null);
        });
        return completableFuture;
    }

    @Override
    public GenerationContext getContext(Instance instance) {
        return generationContextFactory == null ? null : generationContextFactory.newInstance(instance, preGenerationStages);
    }

    private record StageKey(Instance instance, P loc, PreGenerationStage<?> stage) {}
    private record SectionKey(Instance instance, P loc) {}
    private record P(int x, int y, int z) {}
}
