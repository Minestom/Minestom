package net.minestom.server.instance.light;

import net.minestom.server.instance.Chunk;
import net.minestom.server.network.packet.server.play.data.LightData;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class LightDataBuilder {
    private final BitSet skyMask = new BitSet();
    private final BitSet blockMask = new BitSet();
    private final BitSet emptySkyMask = new BitSet();
    private final BitSet emptyBlockMask = new BitSet();
    private final List<byte[]> skyLights = new ArrayList<>();
    private final List<byte[]> blockLights = new ArrayList<>();

    private final int lightSectionCount;
    private int sectionIndex = 0;
    private boolean inSection = false;
    // Skylight data has been set
    private boolean skyHasData = false;
    // Blocklight data has been set
    private boolean blockHasData = false;

    public LightDataBuilder(Chunk chunk) {
        this(chunk.getSections().size());
    }

    public LightDataBuilder(int chunkSectionCount) {
        this.lightSectionCount = chunkSectionCount + 2;
    }

    public LightDataBuilder emptyBlock() {
        if (blockHasData) throw new IllegalStateException("BlockLight has already been set");
        emptyBlockMask.set(sectionIndex);
        blockHasData = true;
        return this;
    }

    public LightDataBuilder blockLight(byte[] sectionData) {
        if (blockHasData) throw new IllegalStateException("BlockLight has already been set");
        if (sectionData == LightCompute.EMPTY_CONTENT) return emptyBlock();
        blockMask.set(sectionIndex);
        blockLights.add(sectionData);
        blockHasData = true;
        return this;
    }

    public LightDataBuilder emptySky() {
        if (skyHasData) throw new IllegalStateException("SkyLight has already been set");
        emptySkyMask.set(sectionIndex);
        skyHasData = true;
        return this;
    }

    public LightDataBuilder skyLight(byte[] sectionData) {
        if (skyHasData) throw new IllegalStateException("SkyLight has already been set");
        if (sectionData == LightCompute.EMPTY_CONTENT) return emptySky();
        skyMask.set(sectionIndex);
        skyLights.add(sectionData);
        skyHasData = true;
        return this;
    }

    public LightDataBuilder beginSection() {
        if (inSection) throw new IllegalStateException("Already began section");
        if (sectionIndex >= lightSectionCount) throw new IllegalStateException("Trying to create too many sections");
        inSection = true;
        return this;
    }

    public LightDataBuilder endSection() {
        if (!inSection) throw new IllegalStateException("Never began section");
        inSection = false;
        sectionIndex++;
        skyHasData = false;
        blockHasData = false;
        return this;
    }

    public LightData build() {
        if (sectionIndex != lightSectionCount) throw new IllegalStateException("Light data was not filled completely");
        return new LightData(skyMask, blockMask, emptySkyMask, emptyBlockMask, skyLights, blockLights);
    }
}
