package net.minestom.server.event.entity.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.RecursiveEvent;
import org.jetbrains.annotations.NotNull;

sealed interface ProjectileCollideEvent<T extends CancellableEvent<T>> extends EntityInstanceEvent, RecursiveEvent, CancellableEvent<T> permits ProjectileCollideWithBlockEvent, ProjectileCollideWithEntityEvent {

    @Override
    default @NotNull Entity entity() {
        return projectile();
    }

    @NotNull Entity projectile();

    @NotNull Pos collisionPosition();
}
