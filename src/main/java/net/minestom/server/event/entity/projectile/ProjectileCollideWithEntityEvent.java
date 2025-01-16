package net.minestom.server.event.entity.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import org.jetbrains.annotations.NotNull;

public record ProjectileCollideWithEntityEvent(
        @NotNull Entity projectile,
        @NotNull Pos collisionPosition,
        @NotNull Entity target,
        boolean cancelled
) implements ProjectileCollideEvent<ProjectileCollideWithEntityEvent> {

    public ProjectileCollideWithEntityEvent(
            @NotNull Entity projectile,
            @NotNull Pos collisionPosition,
            @NotNull Entity target
    ) {
        this(projectile, collisionPosition, target, false);
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator extends EventMutatorCancellable.Simple<ProjectileCollideWithEntityEvent> {

        public Mutator(@NotNull ProjectileCollideWithEntityEvent event) {
            super(event);
        }

        @Override
        public @NotNull ProjectileCollideWithEntityEvent mutated() {
            return new ProjectileCollideWithEntityEvent(this.event.projectile, this.event.collisionPosition, this.event.target, this.isCancelled());
        }
    }
}
