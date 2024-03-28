package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

import static net.minestom.server.instance.Chunk.CHUNK_SIZE_X;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_Z;

public class HeightMapImpl implements HeightMap {
    private final int[] motionBlocking = new int[CHUNK_SIZE_X * CHUNK_SIZE_Z];
    private final int[] surface = new int[CHUNK_SIZE_X * CHUNK_SIZE_Z];
    private final Chunk attachedChunk;
    private final Instance instance;

    private final int minY;
    private final int maxY;

    public HeightMapImpl(Chunk attachedChunk) {
        this.attachedChunk = attachedChunk;
        this.instance = attachedChunk.getInstance();

        minY = instance.getDimensionType().getMinY();
        maxY = instance.getDimensionType().getMaxY();
    }

    @Override
    public void refresh() {
        synchronized (attachedChunk) {
            int startY = HeightMap.getStartY(attachedChunk);

            for (int x = 0; x < CHUNK_SIZE_X; x++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    refreshSurface(x, z, startY, minY);
                    refreshMotionBlocking(x, z, startY, minY);
                }
            }
        }
    }

    @Override
    public void refreshAt(int x, int z) {
        int startY = HeightMap.getStartY(attachedChunk);
        refreshSurface(x, z, startY, minY);
        refreshMotionBlocking(x, z, startY, minY);
    }

    private void refreshSurface(int x, int z, int startY, int minY) {
        int y = startY;
        while (y >= minY) {
            Block block = attachedChunk.getBlock(x, y, z, Block.Getter.Condition.TYPE);
            if (block == null) continue;
            if (!block.isAir()) break;
            y--;
        }
        surface[z << 4 | x] = (y + 1 - minY);
    }

    private void refreshMotionBlocking(int x, int z, int startY, int minY) {
        int y = startY;
        while (y >= minY) {
            Block block = attachedChunk.getBlock(x, y, z, Block.Getter.Condition.TYPE);
            if (block == null) continue;
            if (isMotionBlocking(block)) break;
            y--;
        }
        motionBlocking[z << 4 | x] = (y + 1 - minY);
    }

    private static boolean isMotionBlocking(@NotNull Block block) {
        return block.isSolid() || block.isLiquid() || "true".equals(block.getProperty("waterlogged"));
    }

    @Override
    public NBTCompound getNBT() {
        refresh();

        final int dimensionHeight = instance.getDimensionType().getHeight();
        final int bitsForHeight = MathUtils.bitsToRepresent(dimensionHeight);
        return NBT.Compound(Map.of(
                "MOTION_BLOCKING", NBT.LongArray(HeightMap.encode(motionBlocking, bitsForHeight)),
                "WORLD_SURFACE", NBT.LongArray(HeightMap.encode(surface, bitsForHeight))));
    }
}
