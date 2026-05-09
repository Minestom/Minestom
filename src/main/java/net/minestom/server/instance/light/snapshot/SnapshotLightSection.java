package net.minestom.server.instance.light.snapshot;

import net.minestom.server.instance.Section;
import net.minestom.server.instance.light.LightCompute;
import net.minestom.server.instance.light.LightSection;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SnapshotLightSection implements LightSection<SnapshotLightSection, SnapshotLightSectionType, ChunkData> {
    private final ChunkData chunkData;
    private final @Nullable Section section;
    private final int sectionY;
    private final BlockLight blockLight;
    private final SkyLight skyLight;
    private final AtomicReference<SectionSnapshot> snapshot = new AtomicReference<>(SectionSnapshot.EMPTY);
    private final AtomicInteger version = new AtomicInteger();
    private final AtomicBoolean blockDirty = new AtomicBoolean();
    private final AtomicBoolean skyDirty = new AtomicBoolean();
    private @Nullable SnapshotLightSection up;
    private @Nullable SnapshotLightSection down;

    public SnapshotLightSection(ChunkData chunkData, @Nullable Section section, int sectionY) {
        this.chunkData = chunkData;
        this.section = section;
        this.sectionY = sectionY;
        this.blockLight = new BlockLight(this);
        this.skyLight = new SkyLight();
    }

    public ChunkData chunkData() {
        return chunkData;
    }

    void fullRelightSync() {
        updateSnapshotSync();
        var context = createContext();
        blockLight.relightSync(context);
    }

    public SectionSnapshot snapshot() {
        return snapshot.get();
    }

    private SectionSnapshot updateSnapshotSync() {
        if (section == null) return SectionSnapshot.EMPTY;
        SectionSnapshot snapshot;
        chunkData.chunk.lockReadLock();
        try {
            var blockPalette = section.blockPalette().clone();

            snapshot = new SectionSnapshot(blockPalette);
            this.snapshot.set(snapshot);
        } finally {
            chunkData.chunk.unlockReadLock();
        }
        return snapshot;
    }

    @Override
    public byte[] getBlockLight() {
        return blockLight.get();
    }

    @Override
    public byte[] getSkyLight() {
        return skyLight.get();
    }

    @Override
    public int getBlockLight(int relativeX, int relativeY, int relativeZ) {
        return LightCompute.getLight(getBlockLight(), relativeX, relativeY, relativeZ);
    }

    @Override
    public int getSkyLight(int relativeX, int relativeY, int relativeZ) {
        return LightCompute.getLight(getSkyLight(), relativeX, relativeY, relativeZ);
    }

    @Override
    public void blockChanged(int relativeX, int relativeY, int relativeZ) {
        chunkData.invalidateOcclusionData();

        fullRelightSync();

        chunkData.chunk.scheduleSpecificResend();
    }

    @Override
    public boolean getAndResetResendBlockLight() {
        return blockDirty.compareAndSet(true, false);
    }

    @Override
    public boolean getAndResetResendSkyLight() {
        return skyDirty.compareAndExchange(true, false);
    }

    @Override
    public void copyFrom(SnapshotLightSection section) {

    }

    @Override
    public void neighborLoadUnloadDetected() {
        var context = createContext();
        blockLight.relightSync(context);
    }

    private CalculationContext createContext() {
        return new CalculationContext(chunkData.chunk, sectionY, version.incrementAndGet());
    }

    void scheduleResendBlock() {
        blockDirty.set(true);
        chunkData.chunk.scheduleSpecificResend();
    }

    @Override
    public void initAboveBelow(@Nullable SnapshotLightSection above, @Nullable SnapshotLightSection below) {
        up = above;
        down = below;
    }
}
