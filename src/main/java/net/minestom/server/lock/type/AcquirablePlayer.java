package net.minestom.server.lock.type;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AcquirablePlayer extends AcquirableImpl<Player> {
    public AcquirablePlayer(@NotNull Player value) {
        super(value);
    }
}
