package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

// Microtus - entity meta update
/**
 * Metadata implementation for a Tadpole entity.
 */
public final class TadpoleMeta extends AbstractFishMeta {

    /**
     * Creates a new reference from this meta.
     * @param entity the involved entity
     * @param metadata the involved metadata
     */
    public TadpoleMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }
}
