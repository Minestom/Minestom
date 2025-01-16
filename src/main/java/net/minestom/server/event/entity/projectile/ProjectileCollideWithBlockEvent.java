package net.minestom.server.event.entity.projectile;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public record ProjectileCollideWithBlockEvent(
        @NotNull Entity projectile,
        @NotNull Pos collisionPosition,
        @NotNull Block block,
        boolean cancelled
) implements ProjectileCollideEvent<ProjectileCollideWithBlockEvent>, BlockEvent {

    public ProjectileCollideWithBlockEvent(
            @NotNull Entity projectile,
            @NotNull Pos collisionPosition,
            @NotNull Block block
    ) {
        this(projectile, collisionPosition, block, false);
    }

    public @NotNull Block getBlock() {
        return block;
    }

    @Override
    public @NotNull BlockVec blockPosition() {
        return new BlockVec(collisionPosition);
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator extends EventMutatorCancellable.Simple<ProjectileCollideWithBlockEvent> {

        public Mutator(@NotNull ProjectileCollideWithBlockEvent event) {
            super(event);
        }

        @Override
        public @NotNull ProjectileCollideWithBlockEvent mutated() {
            return new ProjectileCollideWithBlockEvent(this.event.projectile, this.event.collisionPosition, this.event.block, this.isCancelled());
        }
    }
}
