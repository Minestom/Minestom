package net.minestom.server.lock.type;

import net.minestom.server.entity.Player;
import net.minestom.server.lock.AcquirableElement;
import org.jetbrains.annotations.NotNull;

public class AcquirablePlayer implements AcquirableElement<Player> {

    private final Player player;
    private final Handler handler;

    public AcquirablePlayer(@NotNull Player player) {
        this.player = player;
        this.handler = new Handler();
    }

    @Override
    public Player unsafeUnwrap() {
        return player;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }
}
