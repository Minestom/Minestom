package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EndermanMeta extends MonsterMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public EndermanMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public Integer getCarriedBlockID() {
        return super.metadata.getIndex(OFFSET, null);
    }

    public void setCarriedBlockID(@Nullable Integer value) {
        super.metadata.setIndex(OFFSET, Metadata.OptBlockID(value));
    }

    public boolean isScreaming() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    public void setScreaming(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

    public boolean isStaring() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setStaring(boolean value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(value));
    }

}
