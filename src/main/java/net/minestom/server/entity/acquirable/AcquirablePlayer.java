package net.minestom.server.entity.acquirable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.consumer.PlayerConsumer;
import org.jetbrains.annotations.NotNull;

public class AcquirablePlayer extends AcquirableEntity {
    public AcquirablePlayer(@NotNull Entity entity) {
        super(entity);
    }

    public void acquire(@NotNull PlayerConsumer consumer) {
        super.acquire(consumer);
    }

    @Override
    public @NotNull Player unwrap() {
        return (Player) super.unwrap();
    }
}
