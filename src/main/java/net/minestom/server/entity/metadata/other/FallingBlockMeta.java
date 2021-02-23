package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class FallingBlockMeta extends EntityMeta {

    public FallingBlockMeta(@NotNull Entity entity) {
        super(entity);
    }

    public BlockPosition getSpawnPosition() {
        return getMetadata().getIndex((byte) 7, new BlockPosition(0, 0, 0));
    }

    public void setSpawnPosition(BlockPosition value) {
        getMetadata().setIndex((byte) 7, Metadata.Position(value));
    }

}
