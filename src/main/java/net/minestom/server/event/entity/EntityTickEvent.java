package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an entity fireTicks itself.
 * Same event instance used for all tick events for the same entity.
 */
public record EntityTickEvent(@NotNull Entity entity) implements EntityInstanceEvent {}
