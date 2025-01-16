package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player does a left click on an entity or with
 * {@link net.minestom.server.entity.EntityCreature#attack(Entity)}.
 */
public record EntityAttackEvent(@NotNull Entity source, @NotNull Entity target) implements EntityInstanceEvent {

    @Override
    public @NotNull Entity entity() {
        return source;
    }

    /**
     * @return the target of the attack
     */
    @Override
    public @NotNull Entity target() {
        return target;
    }
}
