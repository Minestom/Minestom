package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class WorldSurfaceHeightmap extends Heightmap {
    public WorldSurfaceHeightmap(Chunk attachedChunk) {
        super(attachedChunk);
    }

    @Override
    protected boolean checkBlock(@NotNull Block block) {
        return !block.isAir();
    }

    @Override
    public String NBTName() {
        return "WORLD_SURFACE";
    }
}
