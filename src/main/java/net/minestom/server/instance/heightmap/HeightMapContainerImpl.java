package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.MathUtils;
import org.jglrxavpok.hephaistos.collections.ImmutableLongArray;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.HashMap;
import java.util.Map;

public class HeightMapContainerImpl implements HeightMapContainer {
    private boolean needsCompleteRefresh = true;
    private final Chunk chunk;

    private final Heightmap motionBlocking;
    private final Heightmap worldSurface;

    public HeightMapContainerImpl(Chunk chunk) {
        this.chunk = chunk;

        motionBlocking = new MotionBlockingHeightmap(chunk);
        worldSurface = new WorldSurfaceHeightmap(chunk);
    }

    private void calculateInitial() {
        int startY = Heightmap.getStartY(chunk);

        motionBlocking.refresh(startY);
        worldSurface.refresh(startY);

        needsCompleteRefresh = false;
    }

    @Override
    public void refreshAt(int x, int y, int z, Block block) {
        if (needsCompleteRefresh) calculateInitial();
        motionBlocking.refresh(x, y, z, block);
        worldSurface.refresh(x, y, z, block);
    }

    @Override
    public Heightmap getMotionBlocking() {
        return motionBlocking;
    }

    @Override
    public Heightmap getWorldSurface() {
        return worldSurface;
    }

    @Override
    public NBTCompound getNBT() {
        if (needsCompleteRefresh) calculateInitial();
        return NBT.Compound(Map.of(
                motionBlocking.NBTName(), motionBlocking.getNBT(),
                worldSurface.NBTName(), worldSurface.getNBT()
        ));
    }
}
