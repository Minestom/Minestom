package net.minestom.server.world.generator;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.thread.MinestomThreadPool;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.generator.stages.generation.GenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.PreGenerationStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class WorldGenerator {
    private final static Logger LOGGER = LoggerFactory.getLogger(WorldGenerator.class);
    private final static ExecutorService WORLD_GEN_POOL = new MinestomThreadPool(MinecraftServer.THREAD_COUNT_WORLD_GEN, MinecraftServer.THREAD_NAME_WORLD_GEN);
    private final Map<StageKey, CompletableFuture<Void>> preGenStages = new ConcurrentHashMap<>();
    private final Map<SectionKey, CompletableFuture<Void>> sectionGens = new ConcurrentHashMap<>();
    private final Map<Integer, BiomeGenerator> biomeGenerators;
    private final List<PreGenerationStage> preGenerationStages;
    private final List<GenerationStage> generationStages;

    public WorldGenerator(Set<BiomeGenerator> biomeGenerators, List<PreGenerationStage> preGenerationStages, List<GenerationStage> generationStages) {
        Check.argCondition(biomeGenerators.size() > 0, "At least one BiomeGenerator is required!");
        Map<Integer, BiomeGenerator> bgs = new HashMap<>();
        for (BiomeGenerator biomeGenerator : biomeGenerators) {
            if (bgs.put(biomeGenerator.getId(), biomeGenerator) != null) {
                LOGGER.warn("Multiple generators for biome id {}, overriding previous generator.", biomeGenerator.getId());
            }
        }
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
    }

    public CompletableFuture<Void> generateSection(Instance instance, Palette blockPalette, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
        final SectionKey key = new SectionKey(instance, new Vec(sectionX, sectionY, sectionZ));
        final CompletableFuture<Void> future = sectionGens.get(key);
        return Objects.requireNonNullElseGet(future, () -> generateSection_0(key, blockPalette, biomePalette, sectionX, sectionY, sectionZ));
    }

    private CompletableFuture<Void> generateSection_0(SectionKey key, Palette blockPalette, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        sectionGens.put(key, completableFuture);
        final GenerationContext generationContext = new GenerationContext(this, key.instance());
        for (PreGenerationStage preGenerationStage : preGenerationStages) {
            CompletableFuture<?>[] futures = new CompletableFuture[(int) Math.pow(preGenerationStage.getRange()*2, 3)];
            int i = 0;
            for (int x = -preGenerationStage.getRange(); x < preGenerationStage.getRange(); x++) {
                for (int y = -preGenerationStage.getRange(); y < preGenerationStage.getRange(); y++) {
                    for (int z = -preGenerationStage.getRange(); z < preGenerationStage.getRange(); z++) {
                        futures[i++] = executePreGenerationStage(generationContext, sectionX, sectionY, sectionZ, preGenerationStage);
                    }
                }
            }
            try {
                CompletableFuture.allOf(futures).get();
            } catch (InterruptedException | ExecutionException e) {
                //TODO Error log
                e.printStackTrace();
            }
        }

        WORLD_GEN_POOL.execute(() -> {
            for (GenerationStage generationStage : generationStages) {
                generationStage.process(generationContext, blockPalette, biomePalette, sectionX, sectionY, sectionZ);
            }
            completableFuture.complete(null);
            sectionGens.remove(key);
        });

        return completableFuture;
    }

    private CompletableFuture<Void> executePreGenerationStage(GenerationContext context, int x, int y, int z, PreGenerationStage stage) {
        final StageKey key = new StageKey(context.instance(), new Vec(x, y, z), stage);
        final CompletableFuture<Void> future = preGenStages.get(key);
        if (future != null) {
            return future;
        } else {
            final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            preGenStages.put(key, completableFuture);
            WORLD_GEN_POOL.execute(() -> {
                stage.process(context, x, y, z);
                completableFuture.complete(null);
                preGenStages.remove(key);
            });
            return completableFuture;
        }
    }

    public Map<Integer, BiomeGenerator> getBiomeGenerators() {
        return biomeGenerators;
    }

    private record StageKey(Instance instance, Point loc, PreGenerationStage stage) {}
    private record SectionKey(Instance instance, Point loc) {}
}
