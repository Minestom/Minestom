package net.minestom.server.world.generator;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.thread.MinestomThreadPool;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.generator.stages.generation.GenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.PreGenerationStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class WorldGenerator {
    private final static Logger LOGGER = LoggerFactory.getLogger(WorldGenerator.class);
    private final ExecutorService WORLD_GEN_POOL = new MinestomThreadPool(MinecraftServer.THREAD_COUNT_WORLD_GEN, MinecraftServer.THREAD_NAME_WORLD_GEN);
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

    public boolean generateSection(Instance instance, Palette blockPalette, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
        final GenerationContext generationContext = new GenerationContext(this, instance);
        for (PreGenerationStage preGenerationStage : preGenerationStages) {
            final CountDownLatch countDownLatch = new CountDownLatch(MathUtils.square(preGenerationStage.getRange()));
            for (int x = -preGenerationStage.getRange(); x < preGenerationStage.getRange(); x++) {
                for (int y = -preGenerationStage.getRange(); y < preGenerationStage.getRange(); y++) {
                    for (int z = -preGenerationStage.getRange(); z < preGenerationStage.getRange(); z++) {
                        int finalZ = z;
                        int finalY = y;
                        int finalX = x;
                        WORLD_GEN_POOL.execute(() -> {
                            preGenerationStage.process(generationContext, sectionX + finalX, sectionY + finalY, sectionZ + finalZ);
                            countDownLatch.countDown();
                        });
                    }
                }
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                LOGGER.error("CountDownLatch got interrupted while awaiting pre-generation, halting generation of Section{x={},y={},z={}}", sectionX, sectionY, sectionZ);
                return false;
            }
        }
        for (GenerationStage generationStage : generationStages) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            WORLD_GEN_POOL.execute(() -> {
                generationStage.process(generationContext, blockPalette, biomePalette, sectionX, sectionY, sectionZ);
                future.complete(null);
            });
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Exception while awaiting generation, halting generation of Section{x={},y={},z={}}", sectionX, sectionY, sectionZ);
                return false;
            }
        }
        return true;
    }

    public Map<Integer, BiomeGenerator> getBiomeGenerators() {
        return biomeGenerators;
    }
}
