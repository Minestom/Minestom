package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.PathfinderMobMeta;
import org.jetbrains.annotations.NotNull;

public class AllayMeta extends PathfinderMobMeta {
    public static final byte OFFSET = PathfinderMobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 2;

    public AllayMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }


    public boolean isDancing() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setDancing(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }


    public boolean canDuplicate() {
        return super.metadata.getIndex(OFFSET + 1, true);
    }

    public void setCanDuplicate(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

}
