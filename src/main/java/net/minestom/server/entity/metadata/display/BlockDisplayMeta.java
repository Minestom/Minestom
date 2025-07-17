package net.minestom.server.entity.metadata.display;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class BlockDisplayMeta extends AbstractDisplayMeta {
    public BlockDisplayMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull Block getBlockStateId() {
        return metadata.get(MetadataDef.BlockDisplay.DISPLAYED_BLOCK_STATE);
    }

    public void setBlockState(@NotNull Block value) {
        metadata.set(MetadataDef.BlockDisplay.DISPLAYED_BLOCK_STATE, value);
    }
}
