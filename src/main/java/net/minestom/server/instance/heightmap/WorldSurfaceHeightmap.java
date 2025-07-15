package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;

public class WorldSurfaceHeightmap extends Heightmap {
    public WorldSurfaceHeightmap(Chunk attachedChunk) {
        super(attachedChunk);
    }

    @Override
    protected boolean checkBlock(Block block) {
        return !block.isAir();
    }

    @Override
    public Type type() {
        return Type.WORLD_SURFACE;
    }
}
