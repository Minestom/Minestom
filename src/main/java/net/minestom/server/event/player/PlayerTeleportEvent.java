package net.minestom.server.event.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerTeleportEvent implements PlayerEvent, PlayerInstanceEvent, CancellableEvent {

    private final Player player;

    private Pos pos;
    @Nullable
    private long[] chunks;
    private boolean cancelled = false;

    public PlayerTeleportEvent(@NotNull Player player, @NotNull Pos pos, long @Nullable [] chunks) {
        this.player = player;
        this.pos = pos;
        this.chunks = chunks;
    }

    public Pos getPos() {
        return pos;
    }

    public long[] getChunks() {
        return chunks;
    }

    public void setPos(@NotNull Pos pos) {
        this.pos = pos;
    }

    public void setChunks(@Nullable long[] chunks) {
        this.chunks = chunks;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
