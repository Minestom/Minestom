package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a potion is added to an entity.
 *
 * @param entity the entity who has the potion added
 * @param potion the potion that was added
 */
public record EntityPotionAddEvent(@NotNull Entity entity, @NotNull Potion potion) implements EntityInstanceEvent {}
