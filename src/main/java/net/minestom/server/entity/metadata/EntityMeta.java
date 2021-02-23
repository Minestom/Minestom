package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class EntityMeta {

    protected final Entity entity;

    protected EntityMeta(@NotNull Entity entity) {
        this.entity = entity;
    }

}
