package net.minestom.server.world.generator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.thread.MinestomThreadPool;
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

public class WorldGenerator implements SectionSupplier {
    private final static Logger LOGGER = LoggerFactory.getLogger(WorldGenerator.class);
    private final static ExecutorService WORLD_GEN_POOL = new MinestomThreadPool(MinecraftServer.THREAD_COUNT_WORLD_GEN, MinecraftServer.THREAD_NAME_WORLD_GEN);
    private final Cache<StageKey, CompletableFuture<Void>> preGenStages = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .build();
    private final Cache<SectionKey, CompletableFuture<Void>> sectionGens = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .build();
    private final Map<Integer, BiomeGenerator> biomeGenerators;
    private final List<PreGenerationStage<?>> preGenerationStages;
    private final List<GenerationStage> generationStages;
    private final GenerationContext.Factory<WorldGenerator> generationContextFactory;


    public WorldGenerator(List<GenerationStage> generationStages) {
        this(Collections.emptySet(), Collections.emptyList(), generationStages, null);
    }

    public WorldGenerator(Set<BiomeGenerator> biomeGenerators, List<PreGenerationStage<?>> preGenerationStages, List<GenerationStage> generationStages, GenerationContext.Factory<WorldGenerator> generationContextFactory) {
        Map<Integer, BiomeGenerator> bgs = new HashMap<>();
        for (BiomeGenerator biomeGenerator : biomeGenerators) {
            if (bgs.put(biomeGenerator.getId(), biomeGenerator) != null) {
                LOGGER.warn("Multiple generators for biome id {}, overriding previous generator.", biomeGenerator.getId());
            }
        }
        this.generationContextFactory = generationContextFactory;
        this.biomeGenerators = Collections.unmodifiableMap(bgs);
        this.preGenerationStages = Collections.unmodifiableList(preGenerationStages);
        this.generationStages = Collections.unmodifiableList(generationStages);
        /*
        TODO Do we need to enforce this?
        for (int i = 1; i < generationStages.size(); i++) {
            Check.argCondition(generationStages.get(i-1).getLookAroundRange() < generationStages.get(i).getLookAroundRange(),
                    "Generator stages must be ordered by look around range!");
        }
        */
        Check.argCondition(preGenerationStages.stream()
                        .map(PreGenerationStage::getUniqueId)
                        .collect(Collectors.toSet()).size() != preGenerationStages.size(),
                "Colliding stage IDs!");
    }

    @Override
    public GenerationContext<WorldGenerator> createGenerationContext(Instance instance) {
        return generationContextFactory == null ? null : generationContextFactory.newInstance(this, instance, preGenerationStages);
    }

    @Override
    public CompletableFuture<Void> generateSection(Instance instance, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
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

    private CompletableFuture<Void> executePreGenerationStage(GenerationContext<?> context, int x, int y, int z, PreGenerationStage<?> stage) {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        WORLD_GEN_POOL.execute(() -> {
            stage.process(context, x, y, z);
            completableFuture.complete(null);
        });
        return completableFuture;
    }

    public Map<Integer, BiomeGenerator> getBiomeGenerators() {
        return biomeGenerators;
    }

    private record StageKey(Instance instance, Point loc, PreGenerationStage<?> stage) {}
    private record SectionKey(Instance instance, Point loc) {}
}
