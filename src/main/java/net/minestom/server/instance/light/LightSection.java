package net.minestom.server.instance.light;

import net.minestom.server.instance.Section;
import net.minestom.server.instance.light.LightEngine.WorkTypeTracker;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a section for lighting.
 * A lighting section may be out-of-bounds of the normal world, because we need another section below bedrock for example to propagate light there, even if no blocks can be placed there.
 */
@ApiStatus.Experimental
public class LightSection {
    private final AtomicReference<LightData<byte[]>> skyLight = new AtomicReference<>(new LightData<>(LightCompute.EMPTY_CONTENT, -1));
    private final AtomicReference<LightData<byte[]>> blockLight = new AtomicReference<>(new LightData<>(LightCompute.EMPTY_CONTENT, -1));
    private final AtomicReference<LightData<InternalBlockLight>> blockLightInternal = new AtomicReference<>(new LightData<>(new InternalBlockLight(LightCompute.EMPTY_CONTENT, Palette.blocks()), -1));
    private final AtomicReference<LightData<byte[]>> blockLightExternal = new AtomicReference<>(new LightData<>(LightCompute.EMPTY_CONTENT, -1));
    private final AtomicInteger skyLightVersion = new AtomicInteger();
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
    private final WorkTypeTracker<Integer> trackerBlockLightInternal = new WorkTypeTracker.Hash<>();
    private final WorkTypeTracker<Integer> trackerBlockLightExternal = new WorkTypeTracker.Hash<>();
    private final WorkTypeTracker<Integer> trackerFullBlockRelight = new WorkTypeTracker.Hash<>();
    private final BlockLightSection blockLightSection;

    public LightSection(LightEngine engine, LightingChunk chunk, @Nullable Section chunkSection, int sectionY) {
        this.engine = engine;
        this.chunk = chunk;
        this.chunkSection = chunkSection;
        this.sectionY = sectionY;
        this.sectionX = chunk.getChunkX();
        this.sectionZ = chunk.getChunkZ();
        this.blockLightSection = new BlockLightSection(this);
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
        chunk.scheduleResend();
    }

    public void resendSkyLight() {
        chunk.scheduleResend();
    }

    public LightData<byte[]> getBlockLight() {
        return blockLight.get();
    }

    public LightData<InternalBlockLight> getBlockLightInternal() {
        return blockLightInternal.get();
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

    private int getNextVersion(AtomicInteger version) {
        return version.getAndIncrement();
    }

    public void blockChanged() {
        runAsync(trackerFullBlockRelight, sectionY, this::fullBlockRelight);
    }

    private void bakeAfterRelightAndPropagate() {
        var bakedLightChanged = bakeBlockLight().asBoolean();
        if (bakedLightChanged) {
            resendBlockLight();
            // We need to send to neighbors to recalculate
            scheduleBlockRelight(up);
            scheduleBlockRelight(down);
            var neighborSnapshot = chunk.createNeighborSnapshot();
            scheduleNeighborBlockRelight(neighborSnapshot.east());
            scheduleNeighborBlockRelight(neighborSnapshot.north());
            scheduleNeighborBlockRelight(neighborSnapshot.south());
            scheduleNeighborBlockRelight(neighborSnapshot.west());
        }
    }

    void relightExternalBlockLightAsync() {
        runAsync(trackerBlockLightExternal, sectionY, this::relightExternalBlockLight);
    }

    private void relightExternalBlockLight() {
        if (blockLightSection.relightBlockLightExternal().asBoolean()) {
            bakeAfterRelightAndPropagate();
        }
    }

    private void fullBlockRelight() {
        var modified1 = blockLightSection.relightBlockLightInternal().asBoolean();
        var modified2 = blockLightSection.relightBlockLightExternal().asBoolean();
        if (modified1 || modified2) {
            bakeAfterRelightAndPropagate();
        }
    }

    private void scheduleNeighborBlockRelight(@Nullable LightingChunk neighbor) {
        if (neighbor == null) return;
        scheduleBlockRelight(neighbor.getLightSection(sectionY));
    }

    private void scheduleBlockRelight(@Nullable LightSection section) {
        if (section == null) return;
        section.relightExternalBlockLightAsync();
    }

    /**
     * Relights the internal block light on the caller thread.
     * <p>
     * Used to ensure correct internal block lighting immediately after chunk generation.
     */
    public void generatorRelightBlockLightInternal() {
        // We do not care about modified result here, we bake no matter what
        blockLightSection.relightBlockLightInternal();
    }

    public void generatorRelightBlockLightExternal() {
        blockLightSection.relightBlockLightExternal();
    }

    public CompletableFuture<@Nullable Void> relightSkyLight() {
        return CompletableFuture.completedFuture(null);
    }

    LightUpdateResult<InternalBlockLight> updateBlockLightInternal(LightData<InternalBlockLight> newData) {
        return updateLightData(blockLightInternal, newData, EqualityTester.INTERNAL_BLOCK_LIGHT);
    }

    LightUpdateResult<byte[]> updateBlockLightExternal(LightData<byte[]> newData) {
        return updateLightData(blockLightExternal, newData, EqualityTester.BYTE_ARRAY);
    }

    LightUpdateResult<byte[]> bakeBlockLight() {
        return bake(blockLightInternal, blockLightExternal, blockLight, blockLightVersion);
    }

    private LightUpdateResult<byte[]> bake(AtomicReference<LightData<InternalBlockLight>> input1, AtomicReference<LightData<byte[]>> input2, AtomicReference<LightData<byte[]>> output, AtomicInteger version) {
        var v = getNextVersion(version);
        var l1 = input1.get();
        var l2 = input2.get();
        var data = LightCompute.bake(l1.data().data(), l2.data());
        var lightData = new LightData<>(data, v);
        return updateLightData(output, lightData, EqualityTester.BYTE_ARRAY);
    }

    private interface EqualityTester<T> {
        EqualityTester<byte[]> BYTE_ARRAY = Arrays::equals;
        EqualityTester<InternalBlockLight> INTERNAL_BLOCK_LIGHT = (l1, l2) -> {
            var sameArray = Arrays.equals(l1.data(), l2.data());
            if (!sameArray) return false;
            return l1.palette() == l2.palette() || l1.palette().compare(l2.palette());
        };

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
            if (currentVersion >= newData.version) {
                // Old version, discard
                return new LightUpdateResult<>(LightUpdateResultType.NEWER_VERSION_AVAILABLE, currentData);
            }
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

    public <T> CompletableFuture<@UnknownNullability T> supplyAsync(Callable<@UnknownNullability T> supplier) {
        return engine.scheduleFutureWork(() -> ChunkUtils.isLoaded(chunk), supplier);
    }

    public CompletableFuture<@Nullable Void> runAsync(Runnable runnable) {
        return engine.scheduleFutureWork(() -> ChunkUtils.isLoaded(chunk), () -> {
            runnable.run();
            return null;
        });
    }

    public <WorkKey> CompletableFuture<@Nullable Void> runAsync(WorkTypeTracker<WorkKey> tracker, WorkKey workKey, Runnable runnable) {
        return engine.scheduleFutureWork(tracker, workKey, () -> ChunkUtils.isLoaded(chunk), () -> {
            runnable.run();
            return null;
        });
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

    /**
     * We need to keep the palette for when the array is being used as input to
     * external lighting. We need to query the blocks for occlusion testing.
     */
    public record InternalBlockLight(byte[] data, Palette palette) {
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
