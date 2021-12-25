package net.minestom.server.world.generator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.generator.stages.generation.GenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.PreGenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.StageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        this.preGenerationStages = preGenerationStages.stream().sorted(type.thenComparing(range)).toList();
        if (!this.preGenerationStages.equals(preGenerationStages)) {
            LOGGER.warn("Supplied pre-generation stages were not ordered, they have been automatically rearranged!");
        }
        Check.argCondition(preGenerationStages.stream()
                        .map(PreGenerationStage::getUniqueId)
                        .collect(Collectors.toSet()).size() != preGenerationStages.size(),
                "Colliding stage IDs!");
    }

    @Override
    public List<CompletableFuture<SectionResult>> generateSections(Instance instance, List<Vec> sections) {
        final ArrayList<CompletableFuture<SectionResult>> futures = new ArrayList<>(sections.size());
        for (final Vec pos : sections) {
            final SectionResult result = new SectionResult(new SectionData(new SectionBlockCache(), Palette.biomes()), pos);
            futures.add(generateSection(instance, result.sectionData().blockCache(), result.sectionData().biomePalette(), (int) pos.x(), (int) pos.y(), (int) pos.z())
                    .thenCompose(unused -> CompletableFuture.completedFuture(result)));
        }
        return futures;
    }

    private CompletableFuture<Void> generateSection(Instance instance, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
        return sectionGens.get(new SectionKey(instance, new Vec(sectionX, sectionY, sectionZ)), k -> {
            final CompletableFuture<Void> completableFuture = new CompletableFuture<>();

            // Pre Gen

            for (PreGenerationStage<? extends StageData> preGenerationStage : preGenerationStages) {
                final StageKey key = new StageKey(instance, new Vec(sectionX, sectionY, sectionZ), preGenerationStage);
                CompletableFuture<?>[] futures = switch (preGenerationStage.getType()) {
                    case INSTANCE -> new CompletableFuture<?>[] {
                            preGenStages.get(key, kk -> executePreGenerationStage(instance.getGenerationContext(), 0,0,0, preGenerationStage))
                    };
                    case CHUNK -> {
                        CompletableFuture<?>[] f = new CompletableFuture[MathUtils.square(preGenerationStage.getRange()*2+1)];
                        int i = 0;
                        for (int x = -preGenerationStage.getRange(); x <= preGenerationStage.getRange(); x++) {
                            for (int z = -preGenerationStage.getRange(); z <= preGenerationStage.getRange(); z++) {
                                int finalX = x;
                                int finalZ = z;
                                f[i++] = preGenStages.get(key, kk -> executePreGenerationStage(instance.getGenerationContext(), sectionX+ finalX, 0, sectionZ+ finalZ, preGenerationStage));
                            }
                        }
                        yield f;
                    }
                    case SECTION -> {
                        int min = Math.max(sectionY-preGenerationStage.getRange(), instance.getSectionMinY());
                        int max = Math.min(sectionY+preGenerationStage.getRange(), instance.getSectionMaxY());
                        CompletableFuture<?>[] f = new CompletableFuture[MathUtils.square(preGenerationStage.getRange()*2+1) + (max-min)];
                        int i = 0;
                        for (int x = -preGenerationStage.getRange(); x <= preGenerationStage.getRange(); x++) {
                            for (int y = min; y < max; y++) {
                                for (int z = -preGenerationStage.getRange(); z <= preGenerationStage.getRange(); z++) {
                                    int finalX = x;
                                    int finalY = y;
                                    int finalZ = z;
                                    f[i++] = preGenStages.get(key, kk -> executePreGenerationStage(instance.getGenerationContext(), sectionX+ finalX, finalY, sectionZ+ finalZ, preGenerationStage));
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
                    generationStage.process(instance.getGenerationContext(), blockCache, biomePalette, sectionX, sectionY, sectionZ);
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

    private record StageKey(Instance instance, Point loc, PreGenerationStage<?> stage) {}
    private record SectionKey(Instance instance, Point loc) {}
}
