package net.minestom.server.event.instance;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called by an Instance when an entity is removed from it.
 */
public record RemoveEntityFromInstanceEvent(@NotNull Instance instance, @NotNull Entity entity) implements EntityInstanceEvent {}
