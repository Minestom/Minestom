package net.minestom.server.instance.light;

import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.light.LightEngine.WorkTypeTracker;
import net.minestom.server.instance.light.LightingChunk.Neighbors;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a section for lighting.
 * A lighting section may be out-of-bounds of the normal world, because we need another section below bedrock for example to propagate light there, even if no blocks can be placed there.
 */
@ApiStatus.Experimental
public class LightSection {
    private static final Palette RO_EMPTY_PALETTE = Palette.blocks();
    private static final int[] RO_EMPTY_OCCLUSION_MAP = new int[16 * 16];
    private static final LightData<SectionBlockData> INITIAL_BLOCK_DATA = new LightData<>(new SectionBlockData(RO_EMPTY_PALETTE, RO_EMPTY_OCCLUSION_MAP), 0);
    private static final LightData<byte[]> INITIAL_SKY = new LightData<>(LightCompute.EMPTY_CONTENT, 0);
    private static final LightData<byte[]> INITIAL_BLOCK = new LightData<>(LightCompute.EMPTY_CONTENT, 0);
    private static final LightData<byte[]> INITIAL_BLOCK_INTERNAL = new LightData<>(LightCompute.EMPTY_CONTENT, 0);
    private static final LightData<byte[]> INITIAL_BLOCK_EXTERNAL = new LightData<>(LightCompute.EMPTY_CONTENT, 0);

    private final AtomicReference<LightData<SectionBlockData>> blockData = new AtomicReference<>(INITIAL_BLOCK_DATA);
    private final AtomicReference<LightData<byte[]>> skyLight = new AtomicReference<>(INITIAL_SKY);
    private final AtomicReference<LightData<byte[]>> skyLightInternal = new AtomicReference<>(INITIAL_SKY);
    private final AtomicReference<LightData<byte[]>> skyLightExternal = new AtomicReference<>(INITIAL_SKY);
    private final AtomicReference<LightData<byte[]>> blockLight = new AtomicReference<>(INITIAL_BLOCK);
    private final AtomicReference<LightData<byte[]>> blockLightInternal = new AtomicReference<>(INITIAL_BLOCK_INTERNAL);
    private final AtomicReference<LightData<byte[]>> blockLightExternal = new AtomicReference<>(INITIAL_BLOCK_EXTERNAL);
    // The hope is that 31bit is enough, if it turns out it isn't enough this can be changed to longs.
    // With continuously 1 update/ms in the same section, the 31bit will run out in ~25 days of uptime.
    // 25days is not a huge number, but there shouldn't be 1 update/ms...
    // TODO maybe implement warning messages once version numbers go above 2^30, so server owners are aware of the issue in case it ever happens
    private final AtomicInteger blockDataVersion = new AtomicInteger();
    private final AtomicInteger skyLightVersion = new AtomicInteger();
    private final AtomicInteger skyLightExternalVersion = new AtomicInteger();
    private final AtomicInteger skyLightInternalVersion = new AtomicInteger();
    private final AtomicInteger blockLightVersion = new AtomicInteger();
    private final AtomicInteger blockLightInternalVersion = new AtomicInteger();
    private final AtomicInteger blockLightExternalVersion = new AtomicInteger();
    private final LightEngine engine;
    final LightingChunk chunk;
    final @Nullable Section chunkSection;
    @Nullable LightSection up, down;
    private final int sectionX;
    private final int sectionY;
    private final int sectionZ;
    private final BlockLightSection blockLightSection;
    private final SkyLightSection skyLightSection;
    private final AtomicBoolean resendThisSectionBlockLight = new AtomicBoolean(false);
    private final AtomicBoolean resendThisSectionSkyLight = new AtomicBoolean(false);

    public LightSection(LightEngine engine, LightingChunk chunk, @Nullable Section chunkSection, int sectionY) {
        this.engine = engine;
        this.chunk = chunk;
        this.chunkSection = chunkSection;
        this.sectionY = sectionY;
        this.sectionX = chunk.getChunkX();
        this.sectionZ = chunk.getChunkZ();
        this.blockLightSection = new BlockLightSection(this);
        this.skyLightSection = new SkyLightSection(this);
    }

    public int getBlockLight(int x, int y, int z) {
        return LightCompute.getLight(blockLight.get().data(), x, y, z);
    }

    public int getSkyLight(int x, int y, int z) {
        return LightCompute.getLight(skyLight.get().data(), x, y, z);
    }

    public AtomicBoolean resendThisSectionBlockLight() {
        return resendThisSectionBlockLight;
    }

    public AtomicBoolean resendThisSectionSkyLight() {
        return resendThisSectionSkyLight;
    }

    public int sectionX() {
        return sectionX;
    }

    public int sectionY() {
        return sectionY;
    }

    public int sectionZ() {
        return sectionZ;
    }

    public void resendBlockLight() {
        resendThisSectionBlockLight.set(true);
        chunk.scheduleSpecificResend();
    }

    public void resendSkyLight() {
        resendThisSectionSkyLight.set(true);
        chunk.scheduleSpecificResend();
    }

    public LightData<SectionBlockData> getBlockData() {
        return blockData.get();
    }

    public LightData<byte[]> getBlockLight() {
        return blockLight.get();
    }

    public LightData<byte[]> getBlockLightInternal() {
        return blockLightInternal.get();
    }

    public LightData<byte[]> getSkyLightInternal() {
        return skyLightInternal.get();
    }

    public LightData<byte[]> getSkyLight() {
        return skyLight.get();
    }

    int getNextBlockLightInternalVersion() {
        return getNextVersion(blockLightInternalVersion);
    }

    int getNextBlockLightExternalVersion() {
        return getNextVersion(blockLightExternalVersion);
    }

    int getNextSkyLightExternalVersion() {
        return getNextVersion(skyLightExternalVersion);
    }

    int getNextSkyLightInternalVersion() {
        return getNextVersion(skyLightInternalVersion);
    }

    private int getNextVersion(AtomicInteger version) {
        return version.incrementAndGet();
    }

    public void blockChanged() {
        runAsync(chunk.trackerPalette, sectionY, this::blockChangedInternal);
    }

    private void blockChangedInternal() {
        var result = cloneBlockData();
        if (result.asBoolean()) {
            scheduleFullRelight();
        }
    }

    private LightUpdateResult<SectionBlockData> cloneBlockData() {
        var version = getNextVersion(blockDataVersion);
        Palette blockPalette;
        int[] occlusionMap;
        if (chunkSection != null) {
            chunk.lockReadLock();
            try {
                blockPalette = chunkSection.blockPalette().isEmpty() ? RO_EMPTY_PALETTE : chunkSection.blockPalette().clone();
                occlusionMap = chunk.getOcclusionMap().clone();
            } finally {
                chunk.unlockReadLock();
            }
        } else {
            blockPalette = RO_EMPTY_PALETTE;
            occlusionMap = RO_EMPTY_OCCLUSION_MAP;
        }

        var blockData = new SectionBlockData(blockPalette, occlusionMap);
        return updateLightData(this.blockData, new LightData<>(blockData, version), EqualityTester.BLOCK_DATA);
    }

    private void scheduleFullRelight() {
        runAsync(chunk.trackerFullBlockRelight, sectionY, this::fullBlockRelight);
        runAsync(chunk.trackerFullSkyRelight, sectionY, this::fullSkyRelight);
    }

    private void bakeAfterRelightAndPropagate(int depth) {
        var blockLightBaked = bakeBlockLight();
        var bakedLightChanged = blockLightBaked.asBoolean();
        if (bakedLightChanged) {
            resendBlockLight();
            var oldData = blockLightBaked.oldData().data();
            var newData = blockLight.get().data();
            depth++;
            // We need to tell neighbors to recalculate
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.TOP)) scheduleBlockRelight(up, depth);
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.BOTTOM)) scheduleBlockRelight(down, depth);
            var neighborSnapshot = chunk.createNeighborSnapshot();
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.EAST))
                scheduleNeighborBlockRelight(neighborSnapshot.get(Neighbors.EAST), depth);
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.NORTH))
                scheduleNeighborBlockRelight(neighborSnapshot.get(Neighbors.NORTH), depth);
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.SOUTH))
                scheduleNeighborBlockRelight(neighborSnapshot.get(Neighbors.SOUTH), depth);
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.WEST))
                scheduleNeighborBlockRelight(neighborSnapshot.get(Neighbors.WEST), depth);
        }
        var skyLightBaked = bakeSkyLight();
        var skyLightChanged = skyLightBaked.asBoolean();
        if (skyLightChanged) {
            resendSkyLight();
            var oldData = skyLightBaked.oldData().data();
            var newData = skyLight.get().data();
            depth++;
            // We need to tell neighbors to recalculate
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.TOP)) scheduleSkyRelight(up, depth);
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.BOTTOM)) scheduleSkyRelight(down, depth);
            var neighborSnapshot = chunk.createNeighborSnapshot();
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.EAST))
                scheduleNeighborSkyRelight(neighborSnapshot.get(Neighbors.EAST), depth);
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.NORTH))
                scheduleNeighborSkyRelight(neighborSnapshot.get(Neighbors.NORTH), depth);
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.SOUTH))
                scheduleNeighborSkyRelight(neighborSnapshot.get(Neighbors.SOUTH), depth);
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.WEST))
                scheduleNeighborSkyRelight(neighborSnapshot.get(Neighbors.WEST), depth);
        }
    }

    void relightExternalBlockLightAsync(int depth) {
        runAsync(chunk.trackerBlockLightExternal, sectionY, () -> relightExternalBlockLight(depth));
    }

    void relightExternalSkyLightAsync(int depth) {
        runAsync(chunk.trackerSkyLightExternal, sectionY, () -> relightExternalSkyLight(depth));
    }

    private void relightExternalBlockLight(int depth) {
        if (blockLightSection.relightBlockLightExternal().asBoolean()) {
            bakeAfterRelightAndPropagate(depth);
        }
    }

    private void relightExternalSkyLight(int depth) {
        if (skyLightSection.relightSkyLightExternal().asBoolean()) {
            bakeAfterRelightAndPropagate(depth);
        }
    }

    private void fullSkyRelight() {
        var modifiedInternal = skyLightSection.relightSkyLightInternal().asBoolean();
        var modifiedExternal = skyLightSection.relightSkyLightExternal().asBoolean();
        if (modifiedInternal || modifiedExternal) {
            bakeAfterRelightAndPropagate(0);
        }
    }

    private void fullBlockRelight() {
        var modifiedInternal = blockLightSection.relightBlockLightInternal().asBoolean();
        var modifiedExternal = blockLightSection.relightBlockLightExternal().asBoolean();
        if (modifiedInternal || modifiedExternal) {
            bakeAfterRelightAndPropagate(0);
        }
    }

    private void scheduleNeighborBlockRelight(@Nullable LightingChunk neighbor, int depth) {
        if (neighbor == null) return;
        neighbor.getLightSection(sectionY).relightExternalBlockLightAsync(depth);
    }

    private void scheduleNeighborSkyRelight(@Nullable LightingChunk neighbor, int depth) {
        if (neighbor == null) return;
        neighbor.getLightSection(sectionY).relightExternalSkyLightAsync(depth);
    }

    private void scheduleBlockRelight(@Nullable LightSection section, int depth) {
        if (section == null) return;
        section.relightExternalBlockLightAsync(depth);
    }

    private void scheduleSkyRelight(@Nullable LightSection section, int depth) {
        if (section == null) return;
        section.relightExternalSkyLightAsync(depth);
    }

    /**
     * Clones the block palette on the caller thread
     */
    public void generatorPrepare() {
        cloneBlockData();
    }

    /**
     * Relights the internal block light on the caller thread.
     * <p>
     * Used to ensure correct internal block lighting immediately after chunk generation.
     */
    public void generatorRelightBlockLightInternal() {
        // We do not care about the modified result here, we bake no matter what
        blockLightSection.relightBlockLightInternal();
    }

    /**
     * Relights the internal skylight on the caller thread.
     * <p>
     * Used to ensure correct internal sky lighting immediately after chunk generation.
     */
    public void generatorRelightSkyLightInternal() {
        // We do not care about the modified result here, we bake no matter what
        skyLightSection.relightSkyLightInternal();
    }

    @ApiStatus.Internal
    public static void generatorRelightBlockLightExternalAndBakeSync(List<LightSection> sections) {
        for (var section : sections) {
            // We need to bake once in the beginning to be able to relight external block light
            section.bakeBlockLight();
        }
        // We recalculate the block light until nothing changes.
        var modified = new ArrayList<>(sections);
        var newModified = new ArrayList<LightSection>(modified.size());
        while (!modified.isEmpty()) {
            for (var section : modified) {
                var result = section.blockLightSection.relightBlockLightExternal();
                if (result.asBoolean()) {
                    // External light changed.
                    // Does baking make a difference?
                    if (section.bakeBlockLight().asBoolean()) {
                        // Light changed, we need to recalculate neighbors
                        newModified.add(section);
                    }
                }
            }

            // clear the old modified data and swap
            modified.clear();
            var tmp = modified;
            modified = newModified;
            newModified = tmp;
        }
    }

    LightUpdateResult<byte[]> updateBlockLightInternal(LightData<byte[]> newData) {
        return updateLightData(blockLightInternal, newData, EqualityTester.BYTE_ARRAY);
    }

    LightUpdateResult<byte[]> updateBlockLightExternal(LightData<byte[]> newData) {
        return updateLightData(blockLightExternal, newData, EqualityTester.BYTE_ARRAY);
    }

    LightUpdateResult<byte[]> bakeBlockLight() {
        return bakeLight(blockLightInternal, blockLightExternal, blockLight, blockLightVersion);
    }

    LightUpdateResult<byte[]> updateSkyLightInternal(LightData<byte[]> newData) {
        return updateLightData(skyLightInternal, newData, EqualityTester.BYTE_ARRAY);
    }

    LightUpdateResult<byte[]> updateSkyLightExternal(LightData<byte[]> newData) {
        return updateLightData(skyLightExternal, newData, EqualityTester.BYTE_ARRAY);
    }

    LightUpdateResult<byte[]> bakeSkyLight() {
        return bakeLight(skyLightInternal, skyLightExternal, skyLight, skyLightVersion);
    }

    private LightUpdateResult<byte[]> bakeLight(AtomicReference<LightData<byte[]>> input1, AtomicReference<LightData<byte[]>> input2, AtomicReference<LightData<byte[]>> output, AtomicInteger version) {
        var v = getNextVersion(version);
        var l1 = input1.get();
        var l2 = input2.get();
        var data = LightCompute.bake(l1.data(), l2.data());
        var lightData = new LightData<>(data, v);
        return updateLightData(output, lightData, EqualityTester.BYTE_ARRAY);
    }

    void copyInternalLighting(LightSection other) {
        this.updateBlockLightInternal(other.getBlockLightInternal());
        this.updateSkyLightInternal(other.getSkyLightInternal());
    }

    private interface EqualityTester<T> {
        EqualityTester<byte[]> BYTE_ARRAY = Arrays::equals;
        EqualityTester<SectionBlockData> BLOCK_DATA = SectionBlockData::equals;

        boolean equals(T oldData, T newData);
    }

    /**
     * Updates the current light data (in the AtomicReference) provided we have a newer version.
     *
     * @return if the value was changed. The value not changing means that the provided newData is old and should be discarded.
     */
    private <T> LightUpdateResult<T> updateLightData(AtomicReference<LightData<T>> reference, LightData<T> newData, EqualityTester<T> equalityTester) {
        while (true) {
            var currentData = reference.get();
            var currentVersion = currentData.version;
            if (currentVersion > newData.version) {
                // Old version, discard
                return new LightUpdateResult<>(LightUpdateResultType.NEWER_VERSION_AVAILABLE, currentData);
            } else if (currentVersion == newData.version) throw new IllegalStateException("Version conflict");
            if (reference.compareAndSet(currentData, newData)) {
                // Newer version, update via CAS
                // Return whether the value actually changed. It may simply have been a version bump
                var isEqual = equalityTester.equals(currentData.data(), newData.data());
                if (isEqual) {
                    return new LightUpdateResult<>(LightUpdateResultType.WAS_EQUAL_UPDATED_VERSION, currentData);
                } else {
                    return new LightUpdateResult<>(LightUpdateResultType.UPDATED_DATA, currentData);
                }
            }
        }
    }

    public <WorkKey> CompletableFuture<@Nullable Void> runAsync(WorkTypeTracker<WorkKey> tracker, WorkKey workKey, Runnable runnable) {
        return engine.scheduleFutureWork(tracker, workKey, () -> ChunkUtils.isLoaded(chunk), runnable);
    }

    record LightUpdateResult<T>(LightUpdateResultType type, LightData<T> oldData) {
        boolean asBoolean() {
            return switch (type) {
                case NEWER_VERSION_AVAILABLE, WAS_EQUAL_UPDATED_VERSION -> false;
                case UPDATED_DATA -> true;
            };
        }
    }

    enum LightUpdateResultType {
        NEWER_VERSION_AVAILABLE, WAS_EQUAL_UPDATED_VERSION, UPDATED_DATA
    }

    public static final class LightData<T> {
        private final T data;
        private final int version;

        public LightData(T data, int version) {
            this.data = data;
            this.version = version;
        }

        public T data() {
            return data;
        }
    }
}
