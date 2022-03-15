package net.minestom.server.instance.light;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.light.starlight.SWMRNibbleArray;
import net.minestom.server.instance.light.starlight.StarLightEngine;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public final class ChunkLightData {

    private final @NotNull Chunk chunk;

    @Nullable ChunkTasks tasks = null;
    private boolean lightCorrect;

    private SWMRNibbleArray[] skyNibbles;
    private boolean[] skyEmptinessMap;
    private SWMRNibbleArray[] blockNibbles;
    private boolean[] blockEmptinessMap;

    public ChunkLightData(@NotNull Chunk chunk) {
        this.chunk = chunk;
        clear();
    }

    private ChunkLightData(@NotNull Chunk chunk, boolean lightCorrect, SWMRNibbleArray[] skyNibbles, boolean[] skyEmptinessMap, SWMRNibbleArray[] blockNibbles, boolean[] blockEmptinessMap) {
        this.chunk = chunk;
        this.lightCorrect = lightCorrect;
        this.skyNibbles = skyNibbles;
        this.skyEmptinessMap = skyEmptinessMap;
        this.blockNibbles = blockNibbles;
        this.blockEmptinessMap = blockEmptinessMap;
    }

    public byte[] getSkyLight(final int sectionIndex) {
        return this.skyNibbles[sectionIndex].toVanillaNibble();
    }

    public void setSkyLight(final int sectionIndex, final byte[] light) {
        this.skyNibbles[sectionIndex] = SWMRNibbleArray.fromVanilla(light);
    }

    public byte[] getBlockLight(final int sectionIndex) {
        return this.blockNibbles[sectionIndex].toVanillaNibble();
    }

    public void setBlockLight(final int sectionIndex, final byte[] light) {
        this.blockNibbles[sectionIndex] = SWMRNibbleArray.fromVanilla(light);
    }

    public void clear() {
        lightCorrect = false;
        skyNibbles = chunk.getInstance().getLightManager().hasSkyLight() ? StarLightEngine.getFilledEmptyLight(chunk.getInstance()) : null;
        blockNibbles = chunk.getInstance().getLightManager().hasBlockLight() ? StarLightEngine.getFilledEmptyLight(chunk.getInstance()) : null;
    }

    @ApiStatus.Internal
    public boolean isLightCorrect() {
        return this.lightCorrect;
    }

    @ApiStatus.Internal
    public void setSkyNibbles(@NotNull SWMRNibbleArray[] nibbles) {
//        System.out.println("setting sky nibbles");
        skyNibbles = nibbles;
    }

    @ApiStatus.Internal
    public @NotNull SWMRNibbleArray[] getSkyNibbles() {
        return skyNibbles;
    }

    @ApiStatus.Internal
    public boolean[] getSkyEmptinessMap() {
        return skyEmptinessMap;
    }

    @ApiStatus.Internal
    public void setSkyEmptinessMap(boolean[] skyEmptinessMap) {
        this.skyEmptinessMap = skyEmptinessMap;
    }

    @ApiStatus.Internal
    public void setBlockNibbles(@NotNull SWMRNibbleArray[] nibbles) {
//        System.out.println("setting block nibbles");
        blockNibbles = nibbles;
    }

    @ApiStatus.Internal
    public @NotNull SWMRNibbleArray[] getBlockNibbles() {
        return blockNibbles;
    }

    @ApiStatus.Internal
    public boolean[] getBlockEmptinessMap() {
        return blockEmptinessMap;
    }

    @ApiStatus.Internal
    public void setBlockEmptinessMap(boolean[] blockEmptinessMap) {
        this.blockEmptinessMap = blockEmptinessMap;
    }

    @ApiStatus.Internal
    public CompletableFuture<Chunk> lightChunk(final boolean lit) {
        final InstanceLightManager lightManager = chunk.getInstance().getLightManager();
        Check.notNull(lightManager, "Light manager can't be null during chunk lighting");
        return CompletableFuture.supplyAsync(
                () -> {
                    final Boolean[] emptySections = StarLightEngine.getEmptySectionsForChunk(chunk);
                    if (!lit) {
                        lightCorrect = false;
                        lightManager.lightChunk(chunk, emptySections);
                    } else {
                        lightManager.forceLoadInChunk(chunk, emptySections);
                        lightManager.checkChunkEdges(chunk);
                    }
                    lightCorrect = true;
                    return chunk;
                },
                (runnable) -> lightManager.scheduleChunkLight(this, runnable)
        ).whenComplete((final Chunk c, final Throwable throwable) -> {
            if (throwable != null) {
                MinecraftServer.LOGGER.error("Failed to light chunk " + chunk, throwable);
            }
        });
    }

    public @NotNull ChunkLightData copy(@NotNull Chunk newChunk) {
        return new ChunkLightData(
                newChunk,
                lightCorrect,
                skyNibbles.clone(),
                skyEmptinessMap.clone(),
                blockNibbles.clone(),
                blockEmptinessMap.clone()
        );
    }

}
