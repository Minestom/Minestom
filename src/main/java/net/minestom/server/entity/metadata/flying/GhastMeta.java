package net.minestom.server.entity.metadata.flying;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class GhastMeta extends FlyingMeta {

    public GhastMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isAttacking() {
        return getMetadata().getIndex((byte) 15, false);
    }

    public void setAttacking(boolean value) {
        getMetadata().setIndex((byte) 15, Metadata.Boolean(value));
    }

}
