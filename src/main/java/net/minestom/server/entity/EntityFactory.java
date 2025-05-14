package net.minestom.server.entity;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Used to create {@link Entity} instances, for example when deserializing chunk data.
 */
public interface EntityFactory {
    /**
     * Creates an {@link Entity} instance from a given {@link EntityType} and {@link UUID}
     *
     * @param entityType the entity's {@link EntityType}
     * @param uuid       the entity's {@link UUID}
     * @return a new {@link Entity} instance
     */
    @NotNull Entity create(@NotNull EntityType entityType, @NotNull UUID uuid);
}
