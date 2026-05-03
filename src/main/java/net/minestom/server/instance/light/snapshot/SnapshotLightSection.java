package net.minestom.server.instance.light.snapshot;

import net.minestom.server.instance.Section;
import net.minestom.server.instance.light.LightCompute;
import net.minestom.server.instance.light.LightSection;
import org.jspecify.annotations.Nullable;

public class SnapshotLightSection implements LightSection<SnapshotLightSection, SnapshotLightSectionType, ChunkData> {
    private final ChunkData chunkData;
    private final @Nullable Section section;
    private final int sectionY;
    private final BlockLight blockLight;
    private final SkyLight skyLight;
    private @Nullable SnapshotLightSection up;
    private @Nullable SnapshotLightSection down;

    public SnapshotLightSection(ChunkData chunkData, @Nullable Section section, int sectionY) {
        this.chunkData = chunkData;
        this.section = section;
        this.sectionY = sectionY;
        this.blockLight = new BlockLight();
        this.skyLight = new SkyLight();
    }

    void fullRelightSync() {

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
    }

    @Override
    public boolean getAndResetResendBlockLight() {
        return true;
    }

    @Override
    public boolean getAndResetResendSkyLight() {
        return true;
    }

    @Override
    public void copyFrom(SnapshotLightSection section) {

    }

    @Override
    public void neighborLoadUnloadDetected() {

    }

    @Override
    public void initAboveBelow(@Nullable SnapshotLightSection above, @Nullable SnapshotLightSection below) {
        up = above;
        down = below;
    }
}
