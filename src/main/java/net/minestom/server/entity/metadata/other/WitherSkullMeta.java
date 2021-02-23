package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class WitherSkullMeta extends EntityMeta {

    public WitherSkullMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isInvulnerable() {
        return super.metadata.getIndex((byte) 7, false);
    }

    public void setInvulnerable(boolean value) {
        super.metadata.setIndex((byte) 7, Metadata.Boolean(value));
    }

}
