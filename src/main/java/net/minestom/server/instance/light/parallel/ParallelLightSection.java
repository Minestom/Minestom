package net.minestom.server.instance.light.parallel;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.light.*;
import net.minestom.server.instance.light.LightEngine.WorkTypeTracker;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static net.minestom.server.instance.light.LightCompute.getBlock;

/**
 * Represents a section for lighting.
 * A lighting section may be out-of-bounds of the normal world, because we need another section below bedrock for example to propagate light there, even if no blocks can be placed there.
 */
@ApiStatus.Experimental
public class ParallelLightSection implements LightSection<ParallelLightSection, ParallelLightSection.Type, ChunkData> {
    private static final BlockFace[] FACES = BlockFace.values();
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
    final ChunkData chunkData;
    final @Nullable Section chunkSection;
    @Nullable ParallelLightSection up, down;
    private final int sectionX;
    private final int sectionY;
    private final int sectionZ;
    private final BlockLightSection blockLightSection;
    private final SkyLightSection skyLightSection;
    private final AtomicBoolean resendThisSectionBlockLight = new AtomicBoolean(false);
    private final AtomicBoolean resendThisSectionSkyLight = new AtomicBoolean(false);

    public ParallelLightSection(LightEngine engine, ChunkData chunkData, @Nullable Section chunkSection, int sectionY) {
        this.engine = engine;
        this.chunkData = chunkData;
        this.chunkSection = chunkSection;
        this.sectionY = sectionY;
        this.sectionX = chunkData.getChunkX();
        this.sectionZ = chunkData.getChunkZ();
        this.blockLightSection = new BlockLightSection(this);
        this.skyLightSection = new SkyLightSection(this);
    }

    public static byte[] computeExternal(ParallelLightSection section, Function<ParallelLightSection, byte[]> bakedLightProvider, Function<ParallelLightSection, byte[]> internalLightProvider) {
        // We must always query all our data after acquiring the version ID.
        // This ensures all data is at least as recent as the acquired version.
        var neighborSnapshot = section.chunkData.createNeighborSnapshot();
        var posXC = neighborSnapshot.get(Neighbors.EAST);
        var posZC = neighborSnapshot.get(Neighbors.SOUTH);
        var negXC = neighborSnapshot.get(Neighbors.WEST);
        var negZC = neighborSnapshot.get(Neighbors.NORTH);
        var posX = posXC == null ? null : (ParallelLightSection) posXC.getLightSection(section.sectionY());
        var negX = negXC == null ? null : (ParallelLightSection) negXC.getLightSection(section.sectionY());
        var posZ = posZC == null ? null : (ParallelLightSection) posZC.getLightSection(section.sectionY());
        var negZ = negZC == null ? null : (ParallelLightSection) negZC.getLightSection(section.sectionY());
        var posY = section.up;
        var negY = section.down;
        var content = internalLightProvider.apply(section);
        var blockPalette = section.getBlockData().data().blockPalette();
        var neighbors = new @Nullable ParallelLightSection[]{
                negY, posY, negZ, posZ, negX, posX // Order must be same as order in BlockFace enum
        };
        var lightSources = new ShortArrayFIFOQueue(0);
        for (var i = 0; i < neighbors.length; i++) {
            var neighbor = neighbors[i];
            if (neighbor == null) continue;

            // Light+palette can be out of sync, but that is okay.
            var otherLight = bakedLightProvider.apply(neighbor);
            var otherPalette = neighbor.getBlockData().data().blockPalette();

            final BlockFace face = FACES[i];
            final int k = switch (face) {
                case WEST, BOTTOM, NORTH -> 0;
                case EAST, TOP, SOUTH -> 15;
            };
            for (int bx = 0; bx < 16; bx++) {
                for (int by = 0; by < 16; by++) {

                    final byte neighborLight = switch (face) {
                        case NORTH, SOUTH -> (byte) LightCompute.getLight(otherLight, bx, by, 15 - k);
                        case WEST, EAST -> (byte) LightCompute.getLight(otherLight, 15 - k, bx, by);
                        default -> (byte) LightCompute.getLight(otherLight, bx, 15 - k, by);
                    };
                    // Can't be brighter than this. For the actual brightness we need the opacity,
                    // but we can first check with max values to optimize
                    final byte maxSelfLight = (byte) Math.max(neighborLight - 1, 0);
                    if (maxSelfLight == 0) continue;

                    final int posTo = switch (face) {
                        case NORTH, SOUTH -> bx | (k << 4) | (by << 8);
                        case WEST, EAST -> k | (by << 4) | (bx << 8);
                        default -> bx | (by << 4) | (k << 8);
                    };

                    final Block blockTo = switch (face) {
                        case NORTH, SOUTH -> getBlock(blockPalette, bx, by, k);
                        case WEST, EAST -> getBlock(blockPalette, k, bx, by);
                        default -> getBlock(blockPalette, bx, k, by);
                    };

                    final int opacity = blockTo.registry().lightBlocked();
                    final byte lightEmission = (byte) Math.max(neighborLight - Math.max(opacity, 1), 0);
                    if (lightEmission == 0) continue;
                    if (content != LightCompute.EMPTY_CONTENT) {
                        final int internalEmission = (byte) (Math.max(LightCompute.getLight(content, posTo) - 1, 0));
                        if (lightEmission <= internalEmission) continue;
                    }

                    final Block blockFrom = switch (face) {
                        case NORTH, SOUTH -> getBlock(otherPalette, bx, by, 15 - k);
                        case WEST, EAST -> getBlock(otherPalette, 15 - k, bx, by);
                        default -> getBlock(otherPalette, bx, 15 - k, by);
                    };

                    if (blockFrom.registry().occlusionShape().isOccluded(blockTo.registry().occlusionShape(), face.getOppositeFace()))
                        continue;

                    final int index = posTo | (lightEmission << 12);
                    lightSources.enqueue((short) index);
                }
            }
        }

        return LightCompute.compute(blockPalette, lightSources);
    }

    @Override
    public int getBlockLight(int x, int y, int z) {
        return LightCompute.getLight(blockLight.get().data(), x, y, z);
    }

    @Override
    public int getSkyLight(int x, int y, int z) {
        return LightCompute.getLight(skyLight.get().data(), x, y, z);
    }

    @Override
    public void blockChanged(int relativeX, int relativeY, int relativeZ) {
        blockChanged();
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
        chunkData.chunk.scheduleSpecificResend();
    }

    public void resendSkyLight() {
        resendThisSectionSkyLight.set(true);
        chunkData.chunk.scheduleSpecificResend();
    }

    @Override
    public void neighborLoadUnloadDetected() {
        relightExternalBlockLightAsync(0);
    }

    @Override
    public void initAboveBelow(@Nullable ParallelLightSection above, @Nullable ParallelLightSection below) {
        up = above;
        down = below;
    }

    public LightData<SectionBlockData> getBlockData() {
        return blockData.get();
    }

    public byte[] getBlockLight() {
        return blockLight.get().data();
    }

    public LightData<byte[]> getBlockLightInternal() {
        return blockLightInternal.get();
    }

    public LightData<byte[]> getSkyLightInternal() {
        return skyLightInternal.get();
    }

    public byte[] getSkyLight() {
        return skyLight.get().data();
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
        runAsync(chunkData.trackerPalette, sectionY, this::blockChangedInternal);
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
            chunkData.chunk.lockReadLock();
            try {
                blockPalette = chunkSection.blockPalette().isEmpty() ? RO_EMPTY_PALETTE : chunkSection.blockPalette().clone();
                occlusionMap = chunkData.chunk.getOcclusionMap().clone();
            } finally {
                chunkData.chunk.unlockReadLock();
            }
        } else {
            blockPalette = RO_EMPTY_PALETTE;
            occlusionMap = RO_EMPTY_OCCLUSION_MAP;
        }

        var blockData = new SectionBlockData(blockPalette, occlusionMap);
        return updateLightData(this.blockData, new LightData<>(blockData, version), EqualityTester.BLOCK_DATA);
    }

    private void scheduleFullRelight() {
        runAsync(chunkData.trackerFullBlockRelight, sectionY, this::fullBlockRelight);
        runAsync(chunkData.trackerFullSkyRelight, sectionY, this::fullSkyRelight);
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
            var neighborSnapshot = chunkData.createNeighborSnapshot();
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
            var neighborSnapshot = chunkData.createNeighborSnapshot();
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.EAST))
                scheduleNeighborSkyRelight(neighborSnapshot.get(net.minestom.server.instance.light.Neighbors.EAST), depth);
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.NORTH))
                scheduleNeighborSkyRelight(neighborSnapshot.get(Neighbors.NORTH), depth);
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.SOUTH))
                scheduleNeighborSkyRelight(neighborSnapshot.get(Neighbors.SOUTH), depth);
            if (LightCompute.hasBorderChanged(oldData, newData, BlockFace.WEST))
                scheduleNeighborSkyRelight(neighborSnapshot.get(Neighbors.WEST), depth);
        }
    }

    void relightExternalBlockLightAsync(int depth) {
        runAsync(chunkData.trackerBlockLightExternal, sectionY, () -> relightExternalBlockLight(depth));
    }

    void relightExternalSkyLightAsync(int depth) {
        runAsync(chunkData.trackerSkyLightExternal, sectionY, () -> relightExternalSkyLight(depth));
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
        ((ParallelLightSection) neighbor.getLightSection(sectionY)).relightExternalBlockLightAsync(depth);
    }

    private void scheduleNeighborSkyRelight(@Nullable LightingChunk neighbor, int depth) {
        if (neighbor == null) return;
        ((ParallelLightSection) neighbor.getLightSection(sectionY)).relightExternalSkyLightAsync(depth);
    }

    private void scheduleBlockRelight(@Nullable ParallelLightSection section, int depth) {
        if (section == null) return;
        section.relightExternalBlockLightAsync(depth);
    }

    private void scheduleSkyRelight(@Nullable ParallelLightSection section, int depth) {
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
    public static void generatorRelightBlockLightExternalAndBakeSync(Collection<? extends LightSection> sections) {
        var modified = new ArrayList<>(sections.stream().map(s -> (ParallelLightSection) s).toList());
        for (var section : modified) {
            // We need to bake once in the beginning to be able to relight external block light
            section.bakeBlockLight();
        }
        // We recalculate the block light until nothing changes.
        var newModified = new ArrayList<ParallelLightSection>(modified.size());
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

    void copyInternalLighting(ParallelLightSection other) {
        this.updateBlockLightInternal(other.getBlockLightInternal());
        this.updateSkyLightInternal(other.getSkyLightInternal());
    }

    @Override
    public boolean getAndResetResendBlockLight() {
        return resendThisSectionBlockLight.compareAndSet(true, false);
    }

    @Override
    public boolean getAndResetResendSkyLight() {
        return resendThisSectionSkyLight.compareAndSet(true, false);
    }

    @Override
    public void copyFrom(ParallelLightSection section) {
        copyInternalLighting(section);
        bakeBlockLight();
        bakeSkyLight();
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
        return engine.scheduleFutureWork(tracker, workKey, () -> ChunkUtils.isLoaded(chunkData.chunk), runnable);
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

    public static final class Type implements LightSectionType<ParallelLightSection, ChunkData> {
        public static final Type TYPE = new Type();

        @Override
        public void fullRelightSync(Collection<? extends ParallelLightSection> lightSections) {
            for (var lightSection : lightSections) {
                lightSection.generatorPrepare();
                lightSection.generatorRelightBlockLightInternal();
                lightSection.generatorRelightSkyLightInternal();
            }
            generatorRelightBlockLightExternalAndBakeSync(lightSections);
        }

        @Override
        public ChunkData newChunkData(LightingChunk chunk) {
            return new ChunkData(chunk);
        }

        @Override
        public ParallelLightSection newSection(ChunkData chunkData, @Nullable Section section, int sectionY) {
            return new ParallelLightSection(chunkData.chunk.engine(), chunkData, section, sectionY);
        }
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
