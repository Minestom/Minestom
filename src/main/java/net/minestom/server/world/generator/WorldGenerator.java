package net.minestom.server.world.generator;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.thread.MinestomThreadPool;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class WorldGenerator implements TagHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(WorldGenerator.class);
    private final ExecutorService WORLD_GEN_POOL = new MinestomThreadPool(MinecraftServer.THREAD_COUNT_WORLD_GEN, MinecraftServer.THREAD_NAME_WORLD_GEN);
    private final NBTCompound nbt = new NBTCompound();
    private final Map<Integer, BiomeGenerator> biomeGenerators;
    private final List<WorldGenerationStage> generationStages;

    public WorldGenerator(Set<BiomeGenerator> biomeGenerators, List<WorldGenerationStage> generationStages) {
        Check.argCondition(biomeGenerators.size() > 0, "At least one BiomeGenerator is required!");
        Map<Integer, BiomeGenerator> bgs = new HashMap<>();
        for (BiomeGenerator biomeGenerator : biomeGenerators) {
            if(bgs.put(biomeGenerator.getId(), biomeGenerator) != null) {
                LOGGER.warn("Multiple generators for biome id {}, overriding previous generator.", biomeGenerator.getId());
            }
        }
        this.biomeGenerators = Collections.unmodifiableMap(bgs);
        this.generationStages = Collections.unmodifiableList(generationStages);
        /*
        TODO Do we need to enforce this?
        for (int i = 1; i < generationStages.size(); i++) {
            Check.argCondition(generationStages.get(i-1).getLookAroundRange() < generationStages.get(i).getLookAroundRange(),
                    "Generator stages must be ordered by look around range!");
        }
        */
    }

    public boolean generateChunkData(Instance instance, ChunkBatch batch, int chunkX, int chunkZ) {
        final GenerationContext generationContext = new GenerationContext(this, instance);
        for (WorldGenerationStage generationStage : generationStages) {
            final CountDownLatch countDownLatch = new CountDownLatch(MathUtils.square(generationStage.getLookAroundRange()));
            if (generationStage.getLookAroundRange() > 0) {
                for (int x = -generationStage.getLookAroundRange(); x < generationStage.getLookAroundRange(); x++) {
                    for (int z = -generationStage.getLookAroundRange(); z < generationStage.getLookAroundRange(); z++) {
                        if (x != 0 && z != 0 /*&& TODO check if chunk exists*/) {
                            int finalZ = z;
                            int finalX = x;
                            WORLD_GEN_POOL.execute(() -> {
                                generationStage.lookAround(generationContext, chunkX + finalX, chunkZ + finalZ);
                                countDownLatch.countDown();
                            });
                        }
                    }
                }
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                LOGGER.error("CountDownLatch got interrupted while awaiting lookaround generation, halting generation of Chunk{x={},z={}}", chunkX, chunkZ);
                return false;
            }
            final CompletableFuture<Void> future = new CompletableFuture<>();
            WORLD_GEN_POOL.execute(() -> {
                generationStage.process(generationContext, batch, chunkX, chunkZ);
                future.complete(null);
            });
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Exception occurred while generating Chunk{x={},z={}}", chunkX, chunkZ);
                return false;
            }
        }
        return true;
    }

    public Map<Integer, BiomeGenerator> getBiomeGenerators() {
        return biomeGenerators;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return tag.read(nbt);
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        tag.write(nbt, value);
    }
}
