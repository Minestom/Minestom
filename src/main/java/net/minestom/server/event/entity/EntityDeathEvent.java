package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @param entity
 */
// TODO cause
public record EntityDeathEvent(@NotNull Entity entity) implements EntityInstanceEvent {}
