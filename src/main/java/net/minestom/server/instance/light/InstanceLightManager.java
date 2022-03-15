package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.light.starlight.BlockStarLightEngine;
import net.minestom.server.instance.light.starlight.LightWorldUtil;
import net.minestom.server.instance.light.starlight.SkyStarLightEngine;
import net.minestom.server.utils.cache.LocalCache;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public class InstanceLightManager {

    private final Instance instance;
    private final LocalCache<SkyStarLightEngine> cachedSkyPropagators;
    private final LocalCache<BlockStarLightEngine> cachedBlockPropagators;

    public InstanceLightManager(
            final @NotNull Instance instance,
            final boolean hasSkyLight,
            final boolean hasBlockLight
    ) {
        this.instance = instance;
        this.cachedSkyPropagators = hasSkyLight ? LocalCache.of(() -> new SkyStarLightEngine(instance)) : null;
        this.cachedBlockPropagators = hasBlockLight ? LocalCache.of(() -> new BlockStarLightEngine(instance)) : null;
    }

    public @NotNull Instance getInstance() {
        return instance;
    }

    public boolean hasSkyLight() {
        return cachedSkyPropagators != null;
    }

    public boolean hasBlockLight() {
        return cachedBlockPropagators != null;
    }

    public @NotNull CompletableFuture<Void> onBlockChange(final @NotNull Chunk chunk, final int x, final int y, final int z) {
        final ChunkLightData lightData = chunk.getLightData();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lightData) {
            if (lightData.tasks == null) {
                lightData.tasks = new ChunkTasks();
            }
            lightData.tasks.changesPositions.add(ChunkUtils.getBlockIndex(x, y, z));
            return lightData.tasks.onComplete;
        }
    }

    public @NotNull CompletableFuture<Void> onSectionChange(final @NotNull Chunk chunk, final int sectionIndex, final boolean newEmptyValue) {
        final ChunkLightData lightData = chunk.getLightData();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lightData) {
            if (lightData.tasks == null) {
                lightData.tasks = new ChunkTasks();
            }
            if (lightData.tasks.changedSectionSet == null) {
                lightData.tasks.changedSectionSet = new Boolean[LightWorldUtil.getTotalSections(instance)];
            }
            lightData.tasks.changedSectionSet[sectionIndex - LightWorldUtil.getMinSection(instance)] = newEmptyValue;
            return lightData.tasks.onComplete;
        }
    }

    public void scheduleChunkLight(final @NotNull ChunkLightData lightData, final Runnable runnable) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lightData) {
            if (lightData.tasks == null) {
                lightData.tasks = new ChunkTasks();
            }
            if (lightData.tasks.lightTasks == null) {
                lightData.tasks.lightTasks = new ArrayList<>();
            }
            lightData.tasks.lightTasks.add(runnable);
        }
    }

    public void lightChunk(final @NotNull Chunk chunk, final Boolean[] emptySections) {
        final SkyStarLightEngine skyEngine = cachedSkyPropagators.get();
        final BlockStarLightEngine blockEngine = cachedBlockPropagators.get();
        if (skyEngine != null) {
            skyEngine.light(instance, chunk, emptySections);
        }
        if (blockEngine != null) {
            blockEngine.light(instance, chunk, emptySections);
        }
    }

    public void forceLoadInChunk(final @NotNull Chunk chunk, final Boolean[] emptySections) {
        final SkyStarLightEngine skyEngine = cachedSkyPropagators.get();
        final BlockStarLightEngine blockEngine = cachedBlockPropagators.get();
        if (skyEngine != null) {
            skyEngine.forceHandleEmptySectionChanges(instance, chunk, emptySections);
        }
        if (blockEngine != null) {
            blockEngine.forceHandleEmptySectionChanges(instance, chunk, emptySections);
        }
    }

    public void checkChunkEdges(final @NotNull Chunk chunk) {
        final SkyStarLightEngine skyEngine = cachedSkyPropagators.get();
        final BlockStarLightEngine blockEngine = cachedBlockPropagators.get();
        if (skyEngine != null) {
            skyEngine.checkChunkEdges(instance, chunk.getChunkX(), chunk.getChunkZ());
        }
        if (blockEngine != null) {
            blockEngine.checkChunkEdges(instance, chunk.getChunkX(), chunk.getChunkZ());
        }
    }

    public void removeChunkTasks(final @NotNull Chunk chunk) {
        final ChunkLightData lightData = chunk.getLightData();
        final ChunkTasks tasks;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lightData) {
            if (lightData.tasks == null) {
                return;
            }
            tasks = lightData.tasks;
            lightData.tasks = null;
        }
        tasks.onComplete.complete(null);
    }

    public void runPendingTasks(@NotNull Chunk chunk) {
        final ChunkLightData lightData = chunk.getLightData();
        final ChunkTasks tasks;

        // TODO: sync here is probably slow. Consider switching lightdata#tasks to atomic ref with internal sync?
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lightData) {
            if (lightData.tasks == null) {
                return;
            }
            tasks = lightData.tasks;
            lightData.tasks = null;
        }

        final SkyStarLightEngine skyEngine = cachedSkyPropagators.get();
        final BlockStarLightEngine blockEngine = cachedBlockPropagators.get();
        if (tasks.lightTasks != null) {
            for (final Runnable run : tasks.lightTasks) {
                run.run();
            }
        }
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();
        final IntSet blocks = tasks.changesPositions;
        final Boolean[] sections = tasks.changedSectionSet;
        if (!blocks.isEmpty() || sections != null) {
            if (skyEngine != null) {
                skyEngine.blocksChangedInChunk(instance, chunkX, chunkZ, blocks, sections);
            }
            if (blockEngine != null) {
                blockEngine.blocksChangedInChunk(instance, chunkX, chunkZ, blocks, sections);
            }
        }
        if (skyEngine != null && tasks.queuedEdgeChecksSky != null) {
            skyEngine.checkChunkEdges(instance, chunkX, chunkZ, tasks.queuedEdgeChecksSky);
        }
        if (blockEngine != null && tasks.queuedEdgeChecksBlock != null) {
            blockEngine.checkChunkEdges(instance, chunkX, chunkZ, tasks.queuedEdgeChecksBlock);
        }

        tasks.onComplete.complete(null);
    }
}
