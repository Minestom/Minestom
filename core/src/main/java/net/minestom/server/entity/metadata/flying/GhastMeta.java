package net.minestom.server.entity.metadata.flying;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class GhastMeta extends FlyingMeta {

    public GhastMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isAttacking() {
        return super.metadata.getIndex((byte) 15, false);
    }

    public void setAttacking(boolean value) {
        super.metadata.setIndex((byte) 15, Metadata.Boolean(value));
    }

}
