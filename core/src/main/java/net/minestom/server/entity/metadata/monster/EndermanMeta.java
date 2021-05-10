package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EndermanMeta extends MonsterMeta {

    public EndermanMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public Integer getCarriedBlockID() {
        return super.metadata.getIndex((byte) 15, null);
    }

    public void setCarriedBlockID(@Nullable Integer value) {
        super.metadata.setIndex((byte) 15, Metadata.OptBlockID(value));
    }

    public boolean isScreaming() {
        return super.metadata.getIndex((byte) 16, false);
    }

    public void setScreaming(boolean value) {
        super.metadata.setIndex((byte) 16, Metadata.Boolean(value));
    }

    public boolean isStaring() {
        return super.metadata.getIndex((byte) 17, false);
    }

    public void setStaring(boolean value) {
        super.metadata.setIndex((byte) 17, Metadata.Boolean(value));
    }

}
