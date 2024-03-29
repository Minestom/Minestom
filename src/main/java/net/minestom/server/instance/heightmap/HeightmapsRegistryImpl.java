package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.MathUtils;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

import static net.minestom.server.instance.Chunk.CHUNK_SIZE_X;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_Z;

public class HeightmapsRegistryImpl implements HeightmapsRegistry {
    private final Heightmap motionBlocking;
    private final Heightmap worldSurface;

    private final Instance instance;

    public HeightmapsRegistryImpl(Chunk attachedChunk) {
        instance = attachedChunk.getInstance();

        motionBlocking = new MotionBlockingHeightmap(attachedChunk);
        worldSurface = new WorldSurfaceHeightmap(attachedChunk);

        synchronized (attachedChunk) {
            int startY = Heightmap.getStartY(attachedChunk);

            for (int x = 0; x < CHUNK_SIZE_X; x++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    motionBlocking.refresh(x, z, startY);
                    worldSurface.refresh(x, z, startY);
                }
            }
        }
    }

    @Override
    public void refreshAt(int x, int y, int z, Block block) {
        motionBlocking.refresh(x, y, z, block);
        worldSurface.refresh(x, y, z, block);
    }

    @Override
    public NBTCompound getNBT() {
        final int dimensionHeight = instance.getDimensionType().getHeight();
        final int bitsForHeight = MathUtils.bitsToRepresent(dimensionHeight);
        return NBT.Compound(Map.of(
                "MOTION_BLOCKING", motionBlocking.getNBT(bitsForHeight),
                "WORLD_SURFACE", worldSurface.getNBT(bitsForHeight)));
    }
}
