package net.minestom.server.entity.metadata.display;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class BlockDisplayMeta extends AbstractDisplayMeta {
    public static final byte OFFSET = AbstractDisplayMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public BlockDisplayMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getBlockStateId() {
        return super.metadata.getIndex(OFFSET, Block.AIR.stateId());
    }

    public void setBlockState(int value) {
        super.metadata.setIndex(OFFSET, Metadata.BlockState(value));
    }
}
