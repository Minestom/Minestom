package net.minestom.server.entity.acquirable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AcquirablePlayer extends AcquirableEntity {

    public AcquirablePlayer(@NotNull Entity entity) {
        super(entity);
    }

    @Override
    public @NotNull Acquired<? extends Player> acquire() {
        return (Acquired<? extends Player>) super.acquire();
    }

    @Override
    public @NotNull Player unwrap() {
        return (Player) super.unwrap();
    }
}
