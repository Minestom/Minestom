package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class MotionBlockingHeightmap extends Heightmap {
    public MotionBlockingHeightmap(Chunk attachedChunk) {
        super(attachedChunk);
    }

    @Override
    protected boolean isBreakBlock(@NotNull Block block) {
        return block.isSolid() || block.isLiquid() || "true".equals(block.getProperty("waterlogged"));
    }

    @Override
    public String NBTName() {
        return "MOTION_BLOCKING";
    }
}
