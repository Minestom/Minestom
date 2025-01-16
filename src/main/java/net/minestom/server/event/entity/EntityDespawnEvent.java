package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called right before an entity is removed
 *
 * @param entity the entity who is about to be removed
 */
public record EntityDespawnEvent(@NotNull Entity entity) implements EntityInstanceEvent {}
