package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EndermanMeta extends MonsterMeta {

    public EndermanMeta(@NotNull Entity entity) {
        super(entity);
    }

    public Integer getCarriedBlockID() {
        return getMetadata().getIndex((byte) 15, null);
    }

    public void setCarriedBlockID(@Nullable Integer value) {
        getMetadata().setIndex((byte) 15, Metadata.OptBlockID(value));
    }

    public boolean isScreaming() {
        return getMetadata().getIndex((byte) 16, false);
    }

    public void setScreaming(boolean value) {
        getMetadata().setIndex((byte) 16, Metadata.Boolean(value));
    }

    public boolean isStaring() {
        return getMetadata().getIndex((byte) 17, false);
    }

    public void setStaring(boolean value) {
        getMetadata().setIndex((byte) 17, Metadata.Boolean(value));
    }

}
