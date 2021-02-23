package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class EntityMeta {

    protected final Entity entity;
    protected final Metadata metadata;

    protected EntityMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        this.entity = entity;
        this.metadata = metadata;
    }

}
