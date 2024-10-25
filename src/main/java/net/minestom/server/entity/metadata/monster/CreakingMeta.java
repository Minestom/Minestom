package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class CreakingMeta extends MonsterMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 2;

    public CreakingMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean canMove() {
        return super.metadata.getIndex(OFFSET, true);
    }

    public void setCanMove(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

    public boolean isActive() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    public void setActive(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }
}
